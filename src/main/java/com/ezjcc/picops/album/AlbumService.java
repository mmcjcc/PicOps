package com.ezjcc.picops.album;

import com.ezjcc.picops.user.User;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AlbumService {

    private static final DateTimeFormatter DATE =
        DateTimeFormatter.ofPattern("MMM d, uuuu").withZone(ZoneId.systemDefault());

    private final AlbumRepository albums;

    public AlbumService(AlbumRepository albums) {
        this.albums = albums;
    }

    /** Card data for the album grid, dates pre-formatted for the template. */
    public record Card(UUID id, String title, String visibility, long pictureCount,
                       UUID coverId, String updated) {}

    @Transactional(readOnly = true)
    public List<Card> cardsFor(User owner) {
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : albums.pictureCountsByOwner(owner.getId())) {
            counts.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return albums.findByOwnerIdOrderByUpdatedAtDesc(owner.getId()).stream()
            .map(a -> new Card(a.getId(), a.getTitle(), a.getVisibility().name(),
                counts.getOrDefault(a.getId(), 0L), a.getCoverPictureId(),
                DATE.format(a.getUpdatedAt())))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Card> publicCardsFor(String username) {
        return albums.findByOwnerUsernameIgnoreCaseAndVisibilityOrderByUpdatedAtDesc(
                username, Album.Visibility.PUBLIC).stream()
            .map(a -> new Card(a.getId(), a.getTitle(), a.getVisibility().name(), 0,
                a.getCoverPictureId(), DATE.format(a.getUpdatedAt())))
            .toList();
    }

    /** Album for display: visible to its owner always, to everyone when public. */
    @Transactional(readOnly = true)
    public Album getForView(UUID id, User viewer) {
        Album album = albums.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!album.isPublic() && !album.isOwnedBy(viewer)) {
            // private albums are indistinguishable from missing ones
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return album;
    }

    /** Album for modification: owner only. */
    public Album getOwned(UUID id, User owner) {
        Album album = albums.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!album.isOwnedBy(owner)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return album;
    }

    public Album create(User owner, String title, String description, Album.Visibility visibility) {
        return albums.save(new Album(owner, clean(title, 200, true),
            clean(description, 500, false), visibility));
    }

    public void update(UUID id, User owner, String title, String description,
                       Album.Visibility visibility) {
        Album album = getOwned(id, owner);
        album.setTitle(clean(title, 200, true));
        album.setDescription(clean(description, 500, false));
        album.setVisibility(visibility);
    }

    public void delete(UUID id, User owner) {
        albums.delete(getOwned(id, owner));
    }

    private static String clean(String value, int max, boolean required) {
        String v = value == null ? "" : value.trim();
        if (required && v.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (v.length() > max) {
            v = v.substring(0, max);
        }
        return v.isEmpty() ? null : v;
    }
}
