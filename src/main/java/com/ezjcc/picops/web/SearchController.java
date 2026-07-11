package com.ezjcc.picops.web;

import com.ezjcc.picops.album.Album;
import com.ezjcc.picops.album.AlbumRepository;
import com.ezjcc.picops.picture.PictureRepository;
import com.ezjcc.picops.user.User;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private final AlbumRepository albums;
    private final PictureRepository pictures;
    private final CurrentUser currentUser;
    private final com.ezjcc.picops.ml.MlClient mlClient;
    private final com.ezjcc.picops.ml.MlRepository mlRepo;

    public SearchController(AlbumRepository albums, PictureRepository pictures,
                            CurrentUser currentUser,
                            com.ezjcc.picops.ml.MlClient mlClient,
                            com.ezjcc.picops.ml.MlRepository mlRepo) {
        this.albums = albums;
        this.pictures = pictures;
        this.currentUser = currentUser;
        this.mlClient = mlClient;
        this.mlRepo = mlRepo;
    }

    public record AlbumHit(String id, String title, String visibility) {}
    public record PhotoHit(String id, String label) {}

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q,
                         Principal principal, Model model) {
        User user = currentUser.require(principal);
        String query = q.trim();
        List<AlbumHit> albumHits = List.of();
        List<PhotoHit> photoHits = List.of();
        if (!query.isEmpty()) {
            albumHits = albums.search(user.getId(), query).stream()
                .map(a -> new AlbumHit(a.getId().toString(), a.getTitle(),
                    a.getVisibility().name()))
                .toList();
            photoHits = pictures.searchOwn(user.getId(), query).stream()
                .map(p -> new PhotoHit(p.getId().toString(),
                    p.getTitle() != null ? p.getTitle() : p.getFileName()))
                .toList();
        }
        List<String> visualHits = List.of();
        if (!query.isEmpty()) {
            try {
                String vec = com.ezjcc.picops.ml.MlClient.toVectorLiteral(
                    mlClient.embedText(query));
                visualHits = mlRepo.semanticSearch(user.getId(), vec, 12).stream()
                    .map(Object::toString).toList();
            } catch (Exception e) {
                // sidecar down or not yet warmed up: text search still works
            }
        }
        model.addAttribute("q", query);
        model.addAttribute("albumHits", albumHits);
        model.addAttribute("photoHits", photoHits);
        model.addAttribute("visualHits", visualHits);
        model.addAttribute("initials", CurrentUser.initials(user.getDisplayName()));
        return "search";
    }
}
