package com.ezjcc.picops.web;

import com.ezjcc.picops.ml.FaceRepository;
import com.ezjcc.picops.picture.ThumbnailRepository;
import com.ezjcc.picops.user.User;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.time.Duration;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/** People galleries. Everything here is owner-only: face data never leaves its owner. */
@Controller
public class PeopleController {

    private final FaceRepository faces;
    private final ThumbnailRepository thumbnails;
    private final CurrentUser currentUser;

    public PeopleController(FaceRepository faces, ThumbnailRepository thumbnails,
                            CurrentUser currentUser) {
        this.faces = faces;
        this.thumbnails = thumbnails;
        this.currentUser = currentUser;
    }

    @GetMapping("/people")
    public String people(Principal principal, Model model) {
        User user = currentUser.require(principal);
        model.addAttribute("people", faces.peopleFor(user.getId()));
        model.addAttribute("initials", CurrentUser.initials(user.getDisplayName()));
        return "people";
    }

    @GetMapping("/people/{id}")
    public String person(@PathVariable UUID id, Principal principal, Model model) {
        User user = currentUser.require(principal);
        String name = faces.personNameIfOwned(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("personId", id);
        model.addAttribute("personName", name);
        model.addAttribute("pictures", faces.picturesForPerson(id).stream()
            .map(UUID::toString).toList());
        model.addAttribute("initials", CurrentUser.initials(user.getDisplayName()));
        return "person";
    }

    @PostMapping("/people/{id}/name")
    public String rename(@PathVariable UUID id, Principal principal,
                         @RequestParam String name) {
        User user = currentUser.require(principal);
        faces.personNameIfOwned(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        faces.renamePerson(id, user.getId(), name);
        return "redirect:/people/" + id;
    }

    /** Face crop from the thumbnail, with a margin so it reads as a portrait. */
    @GetMapping("/faces/{id}/crop")
    public ResponseEntity<byte[]> crop(@PathVariable UUID id, Principal principal)
            throws Exception {
        User user = currentUser.require(principal);
        FaceRepository.FaceRow face = faces.face(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!face.ownerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        byte[] thumb = thumbnails.findData(face.pictureId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(thumb));
        int mx = (face.x2() - face.x1()) / 4, my = (face.y2() - face.y1()) / 4;
        int x = Math.max(0, face.x1() - mx);
        int y = Math.max(0, face.y1() - my);
        int w = Math.min(img.getWidth() - x, face.x2() - face.x1() + 2 * mx);
        int h = Math.min(img.getHeight() - y, face.y2() - face.y1() + 2 * my);
        if (w <= 0 || h <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img.getSubimage(x, y, w, h), "jpeg", out);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .cacheControl(CacheControl.maxAge(Duration.ofHours(6)).cachePrivate())
            .body(out.toByteArray());
    }
}
