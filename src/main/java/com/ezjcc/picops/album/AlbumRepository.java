package com.ezjcc.picops.album;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<Album, UUID> {

    List<Album> findByOwnerIdOrderByUpdatedAtDesc(UUID ownerId);

    List<Album> findByOwnerUsernameIgnoreCaseAndVisibilityOrderByUpdatedAtDesc(
        String username, Album.Visibility visibility);

    /** album id -> picture count, without needing the Picture entity loaded. */
    @Query(value = "SELECT a.id, (SELECT count(*) FROM pictures p WHERE p.album_id = a.id) "
                 + "FROM albums a WHERE a.owner_id = :ownerId", nativeQuery = true)
    List<Object[]> pictureCountsByOwner(@Param("ownerId") UUID ownerId);
}
