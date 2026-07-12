package com.ezjcc.picops.web;

import com.ezjcc.picops.album.Album;
import com.ezjcc.picops.comment.CommentService;
import com.ezjcc.picops.picture.PictureService;
import com.ezjcc.picops.user.User;
import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@Controller
public class PictureController {

    private static final java.time.format.DateTimeFormatter TAKEN =
        java.time.format.DateTimeFormatter.ofPattern("MMM d, uuuu")
            .withZone(java.time.ZoneId.systemDefault());

    private final PictureService pictures;
    private final CommentService comments;
    private final CurrentUser currentUser;
    private final com.ezjcc.picops.ml.MlRepository mlRepo;
    private final com.ezjcc.picops.ml.FaceRepository faceRepo;

    public PictureController(PictureService pictures, CommentService comments,
                             CurrentUser currentUser, com.ezjcc.picops.ml.MlRepository mlRepo,
                             com.ezjcc.picops.ml.FaceRepository faceRepo) {
        this.pictures = pictures;
        this.comments = comments;
        this.currentUser = currentUser;
        this.mlRepo = mlRepo;
        this.faceRepo = faceRepo;
    }

    @PostMapping("/albums/{id}/pictures")
    public String upload(@PathVariable UUID id, Principal principal,
                         @RequestParam("files") List<MultipartFile> files) {
        User owner = currentUser.require(principal);
        PictureService.UploadResult result = pictures.upload(id, owner, files);
        if (!result.rejected().isEmpty()) {
            String detail = UriUtils.encodeQueryParam(
                String.join(", ", result.rejected()), "UTF-8");
            return "redirect:/albums/" + id + "?uploaderror=" + detail;
        }
        return "redirect:/albums/" + id;
    }

    @GetMapping("/pictures/{id}")
    public ResponseEntity<byte[]> full(@PathVariable UUID id, Principal principal) {
        User viewer = currentUser.orNull(principal);
        Album album = pictures.albumForPicture(id, viewer);
        // owner gets original bytes (full EXIF); everyone else the stripped variant
        byte[] data = album.isOwnedBy(viewer)
            ? pictures.imageData(id, viewer)
            : pictures.cleanImageData(id, viewer);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(pictures.contentType(id)))
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
            .body(data);
    }

    @GetMapping("/pictures/{id}/thumb")
    public ResponseEntity<byte[]> thumb(@PathVariable UUID id, Principal principal) {
        User viewer = currentUser.orNull(principal);
        byte[] data = pictures.thumbData(id, viewer);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
            .body(data);
    }

    @GetMapping("/pictures/{id}/view")
    public String view(@PathVariable UUID id, Principal principal, Model model) {
        User viewer = currentUser.orNull(principal);
        Album album = pictures.albumForPicture(id, viewer);
        UUID[] neighbors = pictures.neighbors(id, album.getId());
        model.addAttribute("picId", id);
        model.addAttribute("album", album);
        model.addAttribute("isOwner", album.isOwnedBy(viewer));
        model.addAttribute("authenticated", viewer != null);
        if (viewer != null) {
            model.addAttribute("initials", CurrentUser.initials(viewer.getDisplayName()));
        }
        model.addAttribute("prevId", neighbors[0]);
        model.addAttribute("nextId", neighbors[1]);
        model.addAttribute("comments", comments.listForPicture(id, viewer));
        pictures.info(id).ifPresent(info -> {
            StringBuilder meta = new StringBuilder();
            if (info.getTakenAt() != null) {
                meta.append("Taken ").append(TAKEN.format(info.getTakenAt()));
            }
            if (info.getCamera() != null) {
                meta.append(meta.isEmpty() ? "" : " · ").append(info.getCamera());
            }
            model.addAttribute("metaLine", meta.isEmpty() ? null : meta.toString());
        });
        if (album.isOwnedBy(viewer)) {
            // full EXIF can include GPS — owner's eyes only, like the original bytes
            model.addAttribute("metaMap", pictures.fullMetadata(id));
            // face/person data is likewise owner-only
            model.addAttribute("faces", faceRepo.facesForPicture(id));
            // location derives from GPS — owner-only too
            pictures.info(id).ifPresent(info -> {
                String loc = java.util.stream.Stream.of(info.getLocCity(),
                        info.getLocState(), info.getLocCountry())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(java.util.stream.Collectors.joining(", "));
                if (!loc.isEmpty()) {
                    model.addAttribute("locationLine", loc);
                }
            });
        }
        try {
            model.addAttribute("tags", mlRepo.tagsFor(id));
            model.addAttribute("similar", mlRepo.similar(id,
                viewer != null ? viewer.getId() : null, 6).stream()
                .map(Object::toString).toList());
        } catch (Exception e) {
            model.addAttribute("tags", List.of());
            model.addAttribute("similar", List.of());
        }
        return "picture-view";
    }

    @PostMapping("/pictures/{id}/delete")
    public String delete(@PathVariable UUID id, Principal principal) {
        UUID albumId = pictures.delete(id, currentUser.require(principal));
        return "redirect:/albums/" + albumId;
    }
}
