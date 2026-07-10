package com.ezjcc.picops.picture;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PictureRepository extends JpaRepository<Picture, UUID> {

    /** Metadata-only projection so listings never load image bytes. */
    interface Info {
        UUID getId();
        String getFileName();
        String getTitle();
        long getSizeBytes();
        Instant getCreatedAt();
    }

    List<Info> findByAlbumIdOrderByCreatedAtAsc(UUID albumId);

    @Query("select p.id from Picture p where p.album.id = :albumId order by p.createdAt asc")
    List<UUID> idsForAlbum(@Param("albumId") UUID albumId);

    @Query("select p.album.id from Picture p where p.id = :id")
    Optional<UUID> findAlbumId(@Param("id") UUID id);

    @Query("select p.data from Picture p where p.id = :id")
    Optional<byte[]> findData(@Param("id") UUID id);

    @Query("select p.contentType from Picture p where p.id = :id")
    Optional<String> findContentType(@Param("id") UUID id);

    @Query("select coalesce(sum(p.sizeBytes), 0) from Picture p where p.album.owner.id = :ownerId")
    long storageUsed(@Param("ownerId") UUID ownerId);
}
