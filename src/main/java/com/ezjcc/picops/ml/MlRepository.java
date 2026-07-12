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

    private static final UUID NIL = new UUID(0, 0);

    private final JdbcTemplate jdbc;
    private final double searchMaxDistance;
    private final double similarMaxDistance;
    private final double contrastMargin;

    /**
     * Cross-modal CLIP similarities are small in absolute terms: relevant
     * text->image matches land around cosine distance 0.85-0.88, irrelevant
     * ones 0.89+. The absolute ceiling drops clear noise; the caller applies
     * a relative margin against the best hit (see SearchController), which is
     * what actually keeps mountains out of a "beach" search.
     */
    public MlRepository(JdbcTemplate jdbc,
                        @org.springframework.beans.factory.annotation.Value("${picops.ml.search-max-distance:0.92}") double searchMaxDistance,
                        @org.springframework.beans.factory.annotation.Value("${picops.ml.similar-max-distance:0.68}") double similarMaxDistance,
                        @org.springframework.beans.factory.annotation.Value("${picops.ml.contrast-margin:0.11}") double contrastMargin) {
        this.jdbc = jdbc;
        this.searchMaxDistance = searchMaxDistance;
        this.similarMaxDistance = similarMaxDistance;
        this.contrastMargin = contrastMargin;
    }

    public record Hit(UUID id, double distance) {}

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

    /**
     * Nearest pictures to a raw-text query vector, viewer-restricted.
     * Contrast rule: every image is nearer to the generic "a photo" caption
     * than to any specific query, so what separates a real match is HOW MUCH
     * ground it loses — measured on this corpus with raw queries: true
     * matches trail the baseline by ~0.10, irrelevant images by 0.12+.
     * contrastMargin sits between. Raw (unprompted) queries also keep
     * gibberish naturally far from all images.
     */
    public List<Hit> semanticSearch(UUID viewerId, String vectorLiteral,
                                    String baselineLiteral, int limit) {
        return jdbc.query("""
            SELECT p.id, e.embedding <=> ?::vector AS dist FROM pictures p
            JOIN albums a ON a.id = p.album_id
            JOIN picture_embeddings e ON e.picture_id = p.id
            WHERE (a.owner_id = ? OR a.visibility = 'PUBLIC')
              AND e.embedding <=> ?::vector < ?
              AND (e.embedding <=> ?::vector) - (e.embedding <=> ?::vector) < ?
            ORDER BY dist
            LIMIT ?""",
            (rs, i) -> new Hit(rs.getObject(1, UUID.class), rs.getDouble(2)),
            vectorLiteral, viewerId == null ? NIL : viewerId,
            vectorLiteral, searchMaxDistance,
            vectorLiteral, baselineLiteral, contrastMargin, limit);
    }

    /** Visually similar pictures, excluding the picture itself. Image-to-image
     *  distances are much tighter than text-to-image, hence the separate cap. */
    public List<UUID> similar(UUID pictureId, UUID viewerId, int limit) {
        return jdbc.queryForList("""
            SELECT p.id FROM pictures p
            JOIN albums a ON a.id = p.album_id
            JOIN picture_embeddings e ON e.picture_id = p.id
            WHERE p.id <> ?
              AND (a.owner_id = ? OR a.visibility = 'PUBLIC')
              AND e.embedding <=> (SELECT embedding FROM picture_embeddings WHERE picture_id = ?) < ?
            ORDER BY e.embedding <=> (SELECT embedding FROM picture_embeddings WHERE picture_id = ?)
            LIMIT ?""", UUID.class,
            pictureId, viewerId == null ? NIL : viewerId, pictureId,
            similarMaxDistance, pictureId, limit);
    }

    public record GeoTask(UUID pictureId, double lat, double lon) {}

    /** Pictures with GPS but no resolved location yet. */
    public List<GeoTask> pendingGeocode(int limit) {
        return jdbc.query("""
            SELECT id, gps_lat, gps_lon FROM pictures
            WHERE gps_lat IS NOT NULL AND gps_lon IS NOT NULL AND loc_country IS NULL
            LIMIT ?""",
            (rs, i) -> new GeoTask(rs.getObject(1, UUID.class), rs.getDouble(2), rs.getDouble(3)),
            limit);
    }

    public void updateLocation(UUID pictureId, String city, String state, String country) {
        jdbc.update("UPDATE pictures SET loc_city = ?, loc_state = ?, loc_country = ? WHERE id = ?",
            clip(city), clip(state), clip(country), pictureId);
    }

    private static String clip(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.length() > 120 ? s.substring(0, 120) : s;
    }

    public List<String> tagsFor(UUID pictureId) {
        return jdbc.queryForList(
            "SELECT tag FROM picture_tags WHERE picture_id = ? ORDER BY score DESC",
            String.class, pictureId);
    }
}
