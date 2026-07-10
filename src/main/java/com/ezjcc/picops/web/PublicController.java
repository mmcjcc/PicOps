package com.ezjcc.picops.web;

import com.ezjcc.picops.album.AlbumService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicController {

    private final AlbumService albums;
    private final CurrentUser currentUser;

    public PublicController(AlbumService albums, CurrentUser currentUser) {
        this.albums = albums;
        this.currentUser = currentUser;
    }

    /** The shareable page: a user's public albums. Fine to view anonymously. */
    @GetMapping("/u/{username}")
    public String publicAlbums(@PathVariable String username, Principal principal, Model model) {
        List<AlbumService.Card> cards = albums.publicCardsFor(username);
        var viewer = currentUser.orNull(principal);
        model.addAttribute("username", username);
        model.addAttribute("cards", cards);
        model.addAttribute("authenticated", viewer != null);
        if (viewer != null) {
            model.addAttribute("initials", CurrentUser.initials(viewer.getDisplayName()));
        }
        return "public-albums";
    }
}
