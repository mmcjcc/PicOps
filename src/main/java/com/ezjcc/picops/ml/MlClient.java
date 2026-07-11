package com.ezjcc.picops.ml;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Thin HTTP client for the ML sidecar. Callers treat failures as "ML unavailable". */
@Component
public class MlClient {

    public record Tag(String tag, double score) {}
    public record Analysis(List<Double> embedding, List<Tag> tags) {}
    public record TextEmbedding(List<Double> embedding) {}
    public record FaceDet(List<Integer> bbox, double score, List<Double> embedding) {}
    public record Faces(List<FaceDet> faces) {}

    private final RestClient rest;

    public MlClient(@Value("${picops.ml.url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Analysis analyze(byte[] imageBytes) {
        return rest.post().uri("/analyze")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(imageBytes)
            .retrieve()
            .body(Analysis.class);
    }

    public Faces faces(byte[] imageBytes) {
        return rest.post().uri("/faces")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(imageBytes)
            .retrieve()
            .body(Faces.class);
    }

    public List<Double> embedText(String text) {
        TextEmbedding e = rest.post().uri("/embed-text")
            .contentType(MediaType.APPLICATION_JSON)
            .body(java.util.Map.of("text", text))
            .retrieve()
            .body(TextEmbedding.class);
        return e.embedding();
    }

    /** pgvector literal form: [0.1,0.2,...] */
    public static String toVectorLiteral(List<Double> embedding) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) {
                b.append(',');
            }
            b.append(embedding.get(i).floatValue());
        }
        return b.append(']').toString();
    }
}
