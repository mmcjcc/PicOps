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

    /** Face detection ran on 480px-wide thumbnails; bboxes are in that space. */
    private static final int ANALYSIS_WIDTH = 480;
    private static final int CROP_MAX = 320;

    private final FaceRepository faces;
    private final ThumbnailRepository thumbnails;
    private final com.ezjcc.picops.picture.PictureService pictures;
    private final CurrentUser currentUser;

    public PeopleController(FaceRepository faces, ThumbnailRepository thumbnails,
                            com.ezjcc.picops.picture.PictureService pictures,
                            CurrentUser currentUser) {
        this.faces = faces;
        this.thumbnails = thumbnails;
        this.pictures = pictures;
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
        // this person's own face crops (not whole photos), so multi-person
        // photos are never ambiguous about who the cluster means
        model.addAttribute("faces", faces.facesOfPerson(id));
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

    /**
     * Face crop at identification-grade resolution: bbox coords are in
     * thumbnail space, so scale them up and cut from the full-size image
     * (the stripped variant — already orientation-applied, like the
     * thumbnail the detector saw).
     */
    @GetMapping("/faces/{id}/crop")
    public ResponseEntity<byte[]> crop(@PathVariable UUID id, Principal principal)
            throws Exception {
        User user = currentUser.require(principal);
        FaceRepository.FaceRow face = faces.face(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!face.ownerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        byte[] source = pictures.cleanImageData(face.pictureId(), user);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(source));
        if (img == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        double scale = img.getWidth() / (double) Math.min(ANALYSIS_WIDTH, img.getWidth());
        int fx1 = (int) Math.round(face.x1() * scale);
        int fy1 = (int) Math.round(face.y1() * scale);
        int fx2 = (int) Math.round(face.x2() * scale);
        int fy2 = (int) Math.round(face.y2() * scale);
        int mx = (fx2 - fx1) / 4, my = (fy2 - fy1) / 4;
        int x = Math.max(0, fx1 - mx);
        int y = Math.max(0, fy1 - my);
        int w = Math.min(img.getWidth() - x, fx2 - fx1 + 2 * mx);
        int h = Math.min(img.getHeight() - y, fy2 - fy1 + 2 * my);
        if (w <= 0 || h <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        BufferedImage crop = img.getSubimage(x, y, w, h);
        if (crop.getWidth() > CROP_MAX || crop.getHeight() > CROP_MAX) {
            double down = CROP_MAX / (double) Math.max(crop.getWidth(), crop.getHeight());
            int dw = Math.max(1, (int) (crop.getWidth() * down));
            int dh = Math.max(1, (int) (crop.getHeight() * down));
            BufferedImage scaled = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(crop, 0, 0, dw, dh, null);
            g.dispose();
            crop = scaled;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(crop, "jpeg", out);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .cacheControl(CacheControl.maxAge(Duration.ofHours(6)).cachePrivate())
            .body(out.toByteArray());
    }
}
