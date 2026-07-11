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
    private final FaceRepository faceRepo;
    private final MlClient client;
    private final ThumbnailRepository thumbnails;

    public MlWorker(MlRepository repo, FaceRepository faceRepo, MlClient client,
                    ThumbnailRepository thumbnails) {
        this.repo = repo;
        this.faceRepo = faceRepo;
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

    @Scheduled(fixedDelayString = "${picops.ml.interval-ms:15000}", initialDelay = 20000)
    public void faceTick() {
        List<UUID> batch = faceRepo.pendingFaceScan(4);
        int faces = 0;
        for (UUID id : batch) {
            byte[] thumb = thumbnails.findData(id).orElse(null);
            if (thumb == null) {
                continue;
            }
            try {
                MlClient.Faces result = client.faces(thumb);
                UUID owner = faceRepo.ownerOfPicture(id);
                for (MlClient.FaceDet f : result.faces()) {
                    int[] bbox = {f.bbox().get(0), f.bbox().get(1),
                                  f.bbox().get(2), f.bbox().get(3)};
                    faceRepo.storeFace(id, owner, bbox, f.score(),
                        MlClient.toVectorLiteral(f.embedding()));
                    faces++;
                }
                faceRepo.markScanned(id);
            } catch (Exception e) {
                log.info("Face scan unavailable or failed ({}); will retry", e.getMessage());
                return;
            }
        }
        if (faces > 0) {
            log.info("Detected {} face(s) in {} picture(s)", faces, batch.size());
        }
    }
}
