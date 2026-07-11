package com.ezjcc.picops.ml;

import com.ezjcc.picops.picture.ThumbnailRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background analysis: finds pictures without embeddings, ships their
 * thumbnail (small, already oriented, metadata-free) to the sidecar,
 * stores embedding + tags. Sidecar downtime just delays the queue.
 */
@Component
@ConditionalOnProperty(name = "picops.ml.enabled", havingValue = "true", matchIfMissing = true)
public class MlWorker {

    private static final Logger log = LoggerFactory.getLogger(MlWorker.class);

    private final MlRepository repo;
    private final MlClient client;
    private final ThumbnailRepository thumbnails;

    public MlWorker(MlRepository repo, MlClient client, ThumbnailRepository thumbnails) {
        this.repo = repo;
        this.client = client;
        this.thumbnails = thumbnails;
    }

    @Scheduled(fixedDelayString = "${picops.ml.interval-ms:15000}")
    public void tick() {
        List<UUID> batch = repo.pending(8);
        if (batch.isEmpty()) {
            return;
        }
        int done = 0;
        for (UUID id : batch) {
            byte[] thumb = thumbnails.findData(id).orElse(null);
            if (thumb == null) {
                continue;
            }
            try {
                MlClient.Analysis analysis = client.analyze(thumb);
                repo.saveAnalysis(id, MlClient.toVectorLiteral(analysis.embedding()),
                    analysis.tags());
                done++;
            } catch (Exception e) {
                log.info("ML sidecar unavailable or failed ({}); will retry", e.getMessage());
                return; // don't hammer a down sidecar; next tick retries
            }
        }
        if (done > 0) {
            log.info("Analyzed {} picture(s)", done);
        }
    }
}
