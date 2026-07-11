package com.ezjcc.picops.ml;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * pgvector access. Plain SQL via JdbcTemplate — Hibernate has no native
 * vector type, and the queries are simple.
 */
@Repository
public class MlRepository {

    /**
     * Cross-modal CLIP similarities are small in absolute terms: relevant
     * text->image matches typically land at cosine distance 0.85-0.90, so the
     * cutoff only exists to drop far-off noise; ordering does the real work.
     */
    private static final double MAX_DISTANCE = 0.92;

    private static final UUID NIL = new UUID(0, 0);

    private final JdbcTemplate jdbc;

    public MlRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Pictures that have a thumbnail but no embedding yet. */
    public List<UUID> pending(int limit) {
        return jdbc.queryForList("""
            SELECT p.id FROM pictures p
            JOIN thumbnails t ON t.picture_id = p.id
            LEFT JOIN picture_embeddings e ON e.picture_id = p.id
            WHERE e.picture_id IS NULL
            ORDER BY p.created_at DESC LIMIT ?""", UUID.class, limit);
    }

    @Transactional
    public void saveAnalysis(UUID pictureId, String vectorLiteral, List<MlClient.Tag> tags) {
        jdbc.update("""
            INSERT INTO picture_embeddings (picture_id, embedding) VALUES (?, ?::vector)
            ON CONFLICT (picture_id) DO UPDATE SET embedding = EXCLUDED.embedding""",
            pictureId, vectorLiteral);
        jdbc.update("DELETE FROM picture_tags WHERE picture_id = ?", pictureId);
        for (MlClient.Tag tag : tags) {
            jdbc.update("INSERT INTO picture_tags (picture_id, tag, score) VALUES (?, ?, ?)",
                pictureId, tag.tag(), tag.score());
        }
    }

    /** Nearest pictures to a query vector, restricted to what the viewer may see. */
    public List<UUID> semanticSearch(UUID viewerId, String vectorLiteral, int limit) {
        return jdbc.queryForList("""
            SELECT p.id FROM pictures p
            JOIN albums a ON a.id = p.album_id
            JOIN picture_embeddings e ON e.picture_id = p.id
            WHERE (a.owner_id = ? OR a.visibility = 'PUBLIC')
              AND e.embedding <=> ?::vector < ?
            ORDER BY e.embedding <=> ?::vector
            LIMIT ?""", UUID.class,
            viewerId == null ? NIL : viewerId, vectorLiteral, MAX_DISTANCE,
            vectorLiteral, limit);
    }

    /** Visually similar pictures, excluding the picture itself. */
    public List<UUID> similar(UUID pictureId, UUID viewerId, int limit) {
        return jdbc.queryForList("""
            SELECT p.id FROM pictures p
            JOIN albums a ON a.id = p.album_id
            JOIN picture_embeddings e ON e.picture_id = p.id
            WHERE p.id <> ?
              AND (a.owner_id = ? OR a.visibility = 'PUBLIC')
            ORDER BY e.embedding <=> (SELECT embedding FROM picture_embeddings WHERE picture_id = ?)
            LIMIT ?""", UUID.class,
            pictureId, viewerId == null ? NIL : viewerId, pictureId, limit);
    }

    public List<String> tagsFor(UUID pictureId) {
        return jdbc.queryForList(
            "SELECT tag FROM picture_tags WHERE picture_id = ? ORDER BY score DESC",
            String.class, pictureId);
    }
}
