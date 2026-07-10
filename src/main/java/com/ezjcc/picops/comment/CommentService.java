package com.ezjcc.picops.comment;

import com.ezjcc.picops.album.Album;
import com.ezjcc.picops.picture.PictureRepository;
import com.ezjcc.picops.picture.PictureService;
import com.ezjcc.picops.user.User;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CommentService {

    private static final DateTimeFormatter WHEN =
        DateTimeFormatter.ofPattern("MMM d, uuuu HH:mm").withZone(ZoneId.systemDefault());

    private final CommentRepository comments;
    private final PictureRepository pictures;
    private final PictureService pictureService;

    public CommentService(CommentRepository comments, PictureRepository pictures,
                          PictureService pictureService) {
        this.comments = comments;
        this.pictures = pictures;
        this.pictureService = pictureService;
    }

    public record View(UUID id, String author, String when, String body, boolean canDelete) {}

    @Transactional(readOnly = true)
    public List<View> listForPicture(UUID pictureId, User viewer) {
        Album album = pictureService.albumForPicture(pictureId, viewer);
        boolean albumOwner = album.isOwnedBy(viewer);
        return comments.findForPicture(pictureId).stream()
            .map(c -> {
                User author = c.getAuthor();
                boolean mine = viewer != null && author != null
                    && author.getId().equals(viewer.getId());
                return new View(c.getId(),
                    author != null ? author.getDisplayName() : "former member",
                    WHEN.format(c.getCreatedAt()), c.getBody(), mine || albumOwner);
            })
            .toList();
    }

    /** Any signed-in user who can view the picture may comment on it. */
    public UUID add(UUID pictureId, User author, String body) {
        pictureService.albumForPicture(pictureId, author);
        String text = body == null ? "" : body.trim();
        if (text.isEmpty() || text.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "1-1000 characters");
        }
        var picture = pictures.findById(pictureId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return comments.save(new Comment(picture, author, text)).getPicture().getId();
    }

    /** Deletable by its author or by the album owner. */
    public UUID delete(UUID commentId, User user) {
        Comment comment = comments.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UUID pictureId = comment.getPicture().getId();
        boolean mine = comment.getAuthor() != null
            && comment.getAuthor().getId().equals(user.getId());
        boolean albumOwner = comment.getPicture().getAlbum().isOwnedBy(user);
        if (!mine && !albumOwner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        comments.delete(comment);
        return pictureId;
    }
}
