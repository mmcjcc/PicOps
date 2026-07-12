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

    /** Title search across the user's own albums plus anyone's public ones. */
    default List<Album> search(UUID userId, String q) {
        return searchInternal(userId, q, Album.Visibility.PUBLIC);
    }

    @Query("select a from Album a where (a.owner.id = :uid or a.visibility = :pub) "
         + "and lower(a.title) like lower(concat('%', :q, '%')) order by a.updatedAt desc")
    List<Album> searchInternal(@Param("uid") UUID userId, @Param("q") String q,
                               @Param("pub") Album.Visibility pub);

    /** Random public albums with picture counts — the 2005 displaypubalbums, reborn. */
    @Query(value = "SELECT a.id, a.title, a.visibility, a.cover_picture_id, a.updated_at, "
                 + "(SELECT count(*) FROM pictures p WHERE p.album_id = a.id) "
                 + "FROM albums a WHERE a.visibility = 'PUBLIC' "
                 + "ORDER BY random() LIMIT :n", nativeQuery = true)
    List<Object[]> randomPublic(@Param("n") int n);

    @Query(value = "SELECT a.id, a.title, a.visibility, a.cover_picture_id, a.updated_at, "
                 + "(SELECT count(*) FROM pictures p WHERE p.album_id = a.id) "
                 + "FROM albums a WHERE a.visibility = 'PUBLIC' "
                 + "AND a.title ILIKE '%' || :q || '%' "
                 + "ORDER BY a.updated_at DESC LIMIT 24", nativeQuery = true)
    List<Object[]> searchPublic(@Param("q") String q);

    /** album id -> picture count, without needing the Picture entity loaded. */
    @Query(value = "SELECT a.id, (SELECT count(*) FROM pictures p WHERE p.album_id = a.id) "
                 + "FROM albums a WHERE a.owner_id = :ownerId", nativeQuery = true)
    List<Object[]> pictureCountsByOwner(@Param("ownerId") UUID ownerId);
}
