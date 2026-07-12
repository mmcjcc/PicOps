package com.ezjcc.picops.ml;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Faces and per-owner people clusters (pgvector + plain SQL). */
@Repository
public class FaceRepository {

    private final JdbcTemplate jdbc;
    private final double matchDistance;

    public FaceRepository(JdbcTemplate jdbc,
                          @Value("${picops.ml.face-match-distance:0.55}") double matchDistance) {
        this.jdbc = jdbc;
        this.matchDistance = matchDistance;
    }

    public record PersonRow(UUID id, String name, long faceCount, UUID sampleFaceId) {}
    public record FaceRow(UUID id, UUID pictureId, UUID personId, String personName,
                          int x1, int y1, int x2, int y2, UUID ownerId) {}

    public List<UUID> pendingFaceScan(int limit) {
        return jdbc.queryForList("""
            SELECT p.id FROM pictures p
            JOIN thumbnails t ON t.picture_id = p.id
            WHERE p.faces_scanned = false
            ORDER BY p.created_at DESC LIMIT ?""", UUID.class, limit);
    }

    public UUID ownerOfPicture(UUID pictureId) {
        return jdbc.queryForObject("""
            SELECT a.owner_id FROM pictures p JOIN albums a ON a.id = p.album_id
            WHERE p.id = ?""", UUID.class, pictureId);
    }

    /**
     * Incremental clustering: a new face joins the person of its nearest
     * already-assigned face (same owner) when close enough; otherwise it
     * founds a new, unnamed person.
     */
    @Transactional
    public void storeFace(UUID pictureId, UUID ownerId, int[] bbox, double score,
                          String vectorLiteral) {
        UUID personId = jdbc.query("""
            SELECT f.person_id FROM faces f
            JOIN pictures p ON p.id = f.picture_id
            JOIN albums a ON a.id = p.album_id
            WHERE a.owner_id = ? AND f.person_id IS NOT NULL
              AND f.embedding <=> ?::vector < ?
            ORDER BY f.embedding <=> ?::vector LIMIT 1""",
            rs -> rs.next() ? rs.getObject(1, UUID.class) : null,
            ownerId, vectorLiteral, matchDistance, vectorLiteral);
        if (personId == null) {
            personId = jdbc.queryForObject(
                "INSERT INTO people (owner_id) VALUES (?) RETURNING id", UUID.class, ownerId);
        }
        jdbc.update("""
            INSERT INTO faces (picture_id, person_id, x1, y1, x2, y2, det_score, embedding)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?::vector)""",
            pictureId, personId, bbox[0], bbox[1], bbox[2], bbox[3], score, vectorLiteral);
    }

    public void markScanned(UUID pictureId) {
        jdbc.update("UPDATE pictures SET faces_scanned = true WHERE id = ?", pictureId);
    }

    public List<PersonRow> peopleFor(UUID ownerId) {
        return jdbc.query("""
            SELECT pe.id, pe.name, count(f.id) AS faces,
                   (SELECT f2.id FROM faces f2 WHERE f2.person_id = pe.id
                    ORDER BY f2.det_score DESC LIMIT 1) AS sample
            FROM people pe LEFT JOIN faces f ON f.person_id = pe.id
            WHERE pe.owner_id = ?
            GROUP BY pe.id, pe.name
            HAVING count(f.id) > 0
            ORDER BY count(f.id) DESC""",
            (rs, i) -> new PersonRow(rs.getObject(1, UUID.class), rs.getString(2),
                rs.getLong(3), rs.getObject(4, UUID.class)),
            ownerId);
    }

    /** Named people matching a search term, for the owner. */
    public List<PersonRow> searchPeople(UUID ownerId, String q) {
        return jdbc.query("""
            SELECT pe.id, pe.name, count(f.id) AS faces,
                   (SELECT f2.id FROM faces f2 WHERE f2.person_id = pe.id
                    ORDER BY f2.det_score DESC LIMIT 1) AS sample
            FROM people pe LEFT JOIN faces f ON f.person_id = pe.id
            WHERE pe.owner_id = ? AND pe.name ILIKE '%' || ? || '%'
            GROUP BY pe.id, pe.name
            HAVING count(f.id) > 0
            ORDER BY count(f.id) DESC""",
            (rs, i) -> new PersonRow(rs.getObject(1, UUID.class), rs.getString(2),
                rs.getLong(3), rs.getObject(4, UUID.class)),
            ownerId, q);
    }

    public Optional<String> personNameIfOwned(UUID personId, UUID ownerId) {
        List<String> names = jdbc.query(
            "SELECT coalesce(name, '') FROM people WHERE id = ? AND owner_id = ?",
            (rs, i) -> rs.getString(1), personId, ownerId);
        return names.isEmpty() ? Optional.empty() : Optional.of(names.get(0));
    }

    public void renamePerson(UUID personId, UUID ownerId, String name) {
        jdbc.update("UPDATE people SET name = ? WHERE id = ? AND owner_id = ?",
            name == null || name.isBlank() ? null : name.trim(), personId, ownerId);
    }

    public List<UUID> picturesForPerson(UUID personId) {
        return jdbc.queryForList("""
            SELECT DISTINCT p.id FROM pictures p
            JOIN faces f ON f.picture_id = p.id
            WHERE f.person_id = ?""", UUID.class, personId);
    }

    /** All faces belonging to one person, best detections first. */
    public List<FaceRow> facesOfPerson(UUID personId) {
        return jdbc.query("""
            SELECT f.id, f.picture_id, f.person_id, pe.name, f.x1, f.y1, f.x2, f.y2, a.owner_id
            FROM faces f
            JOIN pictures p ON p.id = f.picture_id
            JOIN albums a ON a.id = p.album_id
            LEFT JOIN people pe ON pe.id = f.person_id
            WHERE f.person_id = ?
            ORDER BY f.det_score DESC""",
            (rs, i) -> new FaceRow(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class), rs.getString(4), rs.getInt(5), rs.getInt(6),
                rs.getInt(7), rs.getInt(8), rs.getObject(9, UUID.class)),
            personId);
    }

    public List<FaceRow> facesForPicture(UUID pictureId) {
        return jdbc.query("""
            SELECT f.id, f.picture_id, f.person_id, pe.name, f.x1, f.y1, f.x2, f.y2, a.owner_id
            FROM faces f
            JOIN pictures p ON p.id = f.picture_id
            JOIN albums a ON a.id = p.album_id
            LEFT JOIN people pe ON pe.id = f.person_id
            WHERE f.picture_id = ?
            ORDER BY f.det_score DESC""",
            (rs, i) -> new FaceRow(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class), rs.getString(4), rs.getInt(5), rs.getInt(6),
                rs.getInt(7), rs.getInt(8), rs.getObject(9, UUID.class)),
            pictureId);
    }

    public Optional<FaceRow> face(UUID faceId) {
        List<FaceRow> rows = jdbc.query("""
            SELECT f.id, f.picture_id, f.person_id, pe.name, f.x1, f.y1, f.x2, f.y2, a.owner_id
            FROM faces f
            JOIN pictures p ON p.id = f.picture_id
            JOIN albums a ON a.id = p.album_id
            LEFT JOIN people pe ON pe.id = f.person_id
            WHERE f.id = ?""",
            (rs, i) -> new FaceRow(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class), rs.getString(4), rs.getInt(5), rs.getInt(6),
                rs.getInt(7), rs.getInt(8), rs.getObject(9, UUID.class)),
            faceId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}
