package com.ezjcc.picops.picture;

import com.ezjcc.picops.album.Album;
import com.ezjcc.picops.album.AlbumService;
import com.ezjcc.picops.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PictureService {

    private final PictureRepository pictures;
    private final ThumbnailRepository thumbnails;
    private final AlbumService albums;
    private final ImageService images;
    private final MetadataService metadata;
    private final long quotaBytes;

    public PictureService(PictureRepository pictures, ThumbnailRepository thumbnails,
                          AlbumService albums, ImageService images, MetadataService metadata,
                          @Value("${picops.quota-mb}") long quotaMb) {
        this.pictures = pictures;
        this.thumbnails = thumbnails;
        this.albums = albums;
        this.images = images;
        this.metadata = metadata;
        this.quotaBytes = quotaMb * 1024 * 1024;
    }

    public record UploadResult(int stored, List<String> rejected) {}

    public UploadResult upload(UUID albumId, User owner, List<MultipartFile> files) {
        Album album = albums.getOwned(albumId, owner);
        long used = pictures.storageUsed(owner.getId());
        int stored = 0;
        List<String> rejected = new ArrayList<>();

        for (MultipartFile file : files) {
            String name = cleanFileName(file.getOriginalFilename());
            try {
                byte[] bytes = file.getBytes();
                if (bytes.length == 0) {
                    rejected.add(name + " (empty)");
                    continue;
                }
                if (used + bytes.length > quotaBytes) {
                    rejected.add(name + " (quota exceeded)");
                    continue;
                }
                ImageService.Validated v = images.validate(bytes);
                if (v == null) {
                    rejected.add(name + " (not a supported image)");
                    continue;
                }
                MetadataService.Extracted ex = metadata.extract(bytes);
                var oriented = metadata.orient(v.image(), ex.orientation());
                Picture pic = new Picture(album, v.bytes(), v.contentType(), name, bytes.length);
                pic.setTakenAt(ex.takenAt());
                pic.setCamera(ex.camera());
                pic.setGpsLat(ex.lat());
                pic.setGpsLon(ex.lon());
                pic.setOrientation((short) ex.orientation());
                pic.setMeta(ex.metaJson());
                pic.setCleanData(metadata.stripped(oriented, v.contentType()));
                pic = pictures.save(pic);
                thumbnails.save(new Thumbnail(pic.getId(), images.thumbnailJpeg(oriented)));
                used += bytes.length;
                stored++;
                if (album.getCoverPictureId() == null) {
                    album.setCoverPictureId(pic.getId());
                }
            } catch (Exception e) {
                rejected.add(name + " (error)");
            }
        }
        if (stored > 0) {
            album.touch();
        }
        return new UploadResult(stored, rejected);
    }

    /** Album the picture belongs to, after visibility check for this viewer. */
    @Transactional(readOnly = true)
    public Album albumForPicture(UUID pictureId, User viewer) {
        UUID albumId = pictures.findAlbumId(pictureId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return albums.getForView(albumId, viewer);
    }

    @Transactional(readOnly = true)
    public byte[] imageData(UUID pictureId, User viewer) {
        albumForPicture(pictureId, viewer);
        return pictures.findData(pictureId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public String contentType(UUID pictureId) {
        return pictures.findContentType(pictureId).orElse("image/jpeg");
    }

    /**
     * The metadata-stripped variant for non-owners. Pictures uploaded before
     * this feature get their variant generated and stored on first request.
     */
    public byte[] cleanImageData(UUID pictureId, User viewer) {
        albumForPicture(pictureId, viewer);
        byte[] clean = pictures.findCleanData(pictureId).orElse(null);
        if (clean != null) {
            return clean;
        }
        Picture pic = pictures.findById(pictureId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            ImageService.Validated v = images.validate(pic.getData());
            if (v == null) {
                return pic.getData();
            }
            MetadataService.Extracted ex = metadata.extract(pic.getData());
            clean = metadata.stripped(metadata.orient(v.image(), ex.orientation()),
                v.contentType());
            pic.setCleanData(clean);
            return clean;
        } catch (Exception e) {
            // stripping must fail closed for privacy: no clean variant, no image
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public java.util.Optional<PictureRepository.Info> info(UUID pictureId) {
        return pictures.findInfoById(pictureId);
    }

    @Transactional(readOnly = true)
    public byte[] thumbData(UUID pictureId, User viewer) {
        albumForPicture(pictureId, viewer);
        return thumbnails.findData(pictureId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PictureRepository.Info> listForAlbum(UUID albumId) {
        return pictures.findByAlbumIdOrderByCreatedAtAsc(albumId);
    }

    /** Previous/next picture ids within the album, for slideshow navigation. */
    @Transactional(readOnly = true)
    public UUID[] neighbors(UUID pictureId, UUID albumId) {
        List<UUID> ids = pictures.idsForAlbum(albumId);
        int i = ids.indexOf(pictureId);
        UUID prev = i > 0 ? ids.get(i - 1) : null;
        UUID next = i >= 0 && i < ids.size() - 1 ? ids.get(i + 1) : null;
        return new UUID[] {prev, next};
    }

    public UUID delete(UUID pictureId, User owner) {
        Picture pic = pictures.findById(pictureId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Album album = pic.getAlbum();
        if (!album.isOwnedBy(owner)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        UUID albumId = album.getId();
        pictures.delete(pic);
        if (pictureId.equals(album.getCoverPictureId())) {
            List<UUID> remaining = pictures.idsForAlbum(albumId);
            album.setCoverPictureId(remaining.isEmpty() ? null : remaining.get(0));
        }
        album.touch();
        return albumId;
    }

    private static String cleanFileName(String original) {
        if (original == null || original.isBlank()) {
            return "upload";
        }
        // strip any client path components, keep the last segment only
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }
}
