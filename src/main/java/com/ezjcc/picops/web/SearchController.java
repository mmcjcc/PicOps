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
    private final com.ezjcc.picops.ml.FaceRepository faceRepo;

    public SearchController(AlbumRepository albums, PictureRepository pictures,
                            CurrentUser currentUser,
                            com.ezjcc.picops.ml.MlClient mlClient,
                            com.ezjcc.picops.ml.MlRepository mlRepo,
                            com.ezjcc.picops.ml.FaceRepository faceRepo) {
        this.albums = albums;
        this.pictures = pictures;
        this.currentUser = currentUser;
        this.mlClient = mlClient;
        this.mlRepo = mlRepo;
        this.faceRepo = faceRepo;
    }

    public record AlbumHit(String id, String title, String visibility) {}
    public record PhotoHit(String id, String label) {}

    /** Embedding of a generic caption; matches must beat this to count. */
    private volatile String baselineVec;

    private String baseline() {
        if (baselineVec == null) {
            baselineVec = com.ezjcc.picops.ml.MlClient.toVectorLiteral(
                mlClient.embedText("a photo"));
        }
        return baselineVec;
    }

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
                // raw query, not caption-prompted: prefixing with "a photo of"
                // drags nonsense queries into photo-space and defeats the
                // contrast check; raw gibberish stays naturally far away
                String vec = com.ezjcc.picops.ml.MlClient.toVectorLiteral(
                    mlClient.embedText(query));
                var hits = mlRepo.semanticSearch(user.getId(), vec, baseline(), 12);
                if (!hits.isEmpty()) {
                    // relative cut: keep only results close to the best match,
                    // so weak also-rans (a mountain on a "beach" query) drop out
                    double best = hits.get(0).distance();
                    visualHits = hits.stream()
                        .filter(h -> h.distance() <= best + 0.035)
                        .map(h -> h.id().toString())
                        .toList();
                }
            } catch (Exception e) {
                // sidecar down or not yet warmed up: text search still works
            }
        }
        model.addAttribute("q", query);
        model.addAttribute("albumHits", albumHits);
        model.addAttribute("photoHits", photoHits);
        model.addAttribute("visualHits", visualHits);
        model.addAttribute("peopleHits", query.isEmpty()
            ? List.of() : faceRepo.searchPeople(user.getId(), query));
        model.addAttribute("initials", CurrentUser.initials(user.getDisplayName()));
        return "search";
    }
}
