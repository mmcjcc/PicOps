package com.ezjcc.picops.web;

import com.ezjcc.picops.album.Album;
import com.ezjcc.picops.album.AlbumService;
import com.ezjcc.picops.picture.PictureService;
import com.ezjcc.picops.user.User;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AlbumController {

    private final AlbumService albums;
    private final PictureService pictures;
    private final CurrentUser currentUser;

    public AlbumController(AlbumService albums, PictureService pictures, CurrentUser currentUser) {
        this.albums = albums;
        this.pictures = pictures;
        this.currentUser = currentUser;
    }

    @GetMapping("/")
    public String home(Principal principal,
                       @RequestParam(required = false) String q, Model model) {
        User user = currentUser.orNull(principal);
        if (user == null) {
            // anonymous landing: random public albums + public title search
            String query = q == null ? "" : q.trim();
            model.addAttribute("q", query);
            model.addAttribute("cards", query.isEmpty()
                ? albums.publicRandomCards(12)
                : albums.publicSearchCards(query));
            return "explore";
        }
        model.addAttribute("displayName", user.getDisplayName());
        model.addAttribute("initials", CurrentUser.initials(user.getDisplayName()));
        model.addAttribute("cards", albums.cardsFor(user));
        model.addAttribute("discover", albums.publicRandomCards(8));
        return "home";
    }

    @GetMapping("/albums/new")
    public String createForm(Principal principal, Model model) {
        currentUser.require(principal);
        model.addAttribute("mode", "create");
        return "album-form";
    }

    @PostMapping("/albums")
    public String create(Principal principal,
                         @RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "PRIVATE") Album.Visibility visibility) {
        Album album = albums.create(currentUser.require(principal), title, description, visibility);
        return "redirect:/albums/" + album.getId();
    }

    @GetMapping("/albums/{id}")
    public String view(@PathVariable UUID id, Principal principal, Model model) {
        User viewer = currentUser.orNull(principal);
        Album album = albums.getForView(id, viewer);
        boolean owner = album.isOwnedBy(viewer);
        model.addAttribute("album", album);
        model.addAttribute("isOwner", owner);
        model.addAttribute("authenticated", viewer != null);
        if (viewer != null) {
            model.addAttribute("initials", CurrentUser.initials(viewer.getDisplayName()));
        }
        model.addAttribute("pictures", pictures.listForAlbum(album.getId()));
        return "album";
    }

    @GetMapping("/albums/{id}/edit")
    public String editForm(@PathVariable UUID id, Principal principal, Model model) {
        Album album = albums.getOwned(id, currentUser.require(principal));
        model.addAttribute("mode", "edit");
        model.addAttribute("album", album);
        return "album-form";
    }

    @PostMapping("/albums/{id}")
    public String update(@PathVariable UUID id, Principal principal,
                         @RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "PRIVATE") Album.Visibility visibility) {
        albums.update(id, currentUser.require(principal), title, description, visibility);
        return "redirect:/albums/" + id;
    }

    @PostMapping("/albums/{id}/delete")
    public String delete(@PathVariable UUID id, Principal principal) {
        albums.delete(id, currentUser.require(principal));
        return "redirect:/";
    }
}
