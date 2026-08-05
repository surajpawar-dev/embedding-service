package com.suraj.rag.embedding.adapter.outbound;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import com.suraj.rag.embedding.dto.DocumentEmbeddingResponse;
import com.suraj.rag.embedding.dto.EmbeddingResponse;
import com.suraj.rag.embedding.port.outbound.EmbeddingJobStorePort;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "job-store", name = "mode", havingValue = "in-memory")
public class InMemoryEmbeddingJobStoreAdapter implements EmbeddingJobStorePort {

    private final Map<UUID, EmbeddingResponse> responsesByChunkId = new ConcurrentHashMap<>();
    private final Map<UUID, DocumentEmbeddingResponse> jobsById = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> latestJobIdByDocumentId = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicInteger> completedByJobId = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicInteger> failedByJobId = new ConcurrentHashMap<>();

    @Override
    public UUID createJob(
            UUID documentId,
            int totalChunks,
            String embeddingModel,
            int embeddingDimension,
            String correlationId) {
        UUID jobId = UUID.randomUUID();
        jobsById.put(
                jobId,
                new DocumentEmbeddingResponse(
                        jobId,
                        documentId,
                        totalChunks,
                        null,
                        embeddingModel,
                        embeddingDimension,
                        EmbeddingStatus.RECEIVED,
                        Instant.now()));
        latestJobIdByDocumentId.put(documentId, jobId);
        completedByJobId.put(jobId, new AtomicInteger());
        failedByJobId.put(jobId, new AtomicInteger());
        return jobId;
    }

    @Override
    public void markChunk(
            UUID jobId,
            UUID documentId,
            UUID chunkId,
            UUID embeddingId,
            String model,
            int dimension,
            String checksum,
            EmbeddingStatus status) {
        responsesByChunkId.put(
                chunkId,
                new EmbeddingResponse(
                        jobId,
                        documentId,
                        chunkId,
                        embeddingId,
                        model,
                        dimension,
                        status,
                        Instant.now()));
        if (status == EmbeddingStatus.COMPLETED || status == EmbeddingStatus.READY) {
            completedByJobId.get(jobId).incrementAndGet();
        } else if (status == EmbeddingStatus.FAILED) {
            failedByJobId.get(jobId).incrementAndGet();
        }
    }

    @Override
    public void completeJob(UUID jobId, EmbeddingStatus status) {
        DocumentEmbeddingResponse existing = jobsById.get(jobId);
        if (existing != null) {
            jobsById.put(
                    jobId,
                    new DocumentEmbeddingResponse(
                            jobId,
                            existing.documentId(),
                            existing.acceptedChunks(),
                            existing.documentChecksum(),
                            existing.embeddingModel(),
                            existing.embeddingDimension(),
                            status,
                            existing.createdAt()));
        }
    }

    @Override
    public void failJob(UUID jobId, String failureReason) {
        completeJob(jobId, EmbeddingStatus.FAILED);
    }

    @Override
    public Optional<EmbeddingResponse> findByChunkId(UUID chunkId) {
        return Optional.ofNullable(responsesByChunkId.get(chunkId));
    }

    @Override
    public Optional<DocumentEmbeddingResponse> findLatestByDocumentId(UUID documentId) {
        return Optional.ofNullable(latestJobIdByDocumentId.get(documentId)).map(jobsById::get);
    }

    @Override
    public long countByDocumentIdAndStatus(UUID documentId, EmbeddingStatus status) {
        return responsesByChunkId.values().stream()
                .filter(response -> response.documentId().equals(documentId))
                .filter(response -> response.status() == status)
                .count();
    }

    @Override
    public long countByDocumentId(UUID documentId) {
        return responsesByChunkId.values().stream()
                .filter(response -> response.documentId().equals(documentId))
                .count();
    }
}
