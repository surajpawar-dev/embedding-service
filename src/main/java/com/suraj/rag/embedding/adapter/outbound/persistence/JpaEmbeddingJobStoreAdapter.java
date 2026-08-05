package com.suraj.rag.embedding.adapter.outbound.persistence;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import com.suraj.rag.embedding.dto.DocumentEmbeddingResponse;
import com.suraj.rag.embedding.dto.EmbeddingResponse;
import com.suraj.rag.embedding.entity.EmbeddingJobEntity;
import com.suraj.rag.embedding.entity.EmbeddingStatusEntity;
import com.suraj.rag.embedding.port.outbound.EmbeddingJobStorePort;
import com.suraj.rag.embedding.repository.EmbeddingJobJpaRepository;
import com.suraj.rag.embedding.repository.EmbeddingStatusJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "job-store", name = "mode", havingValue = "jpa")
public class JpaEmbeddingJobStoreAdapter implements EmbeddingJobStorePort {

    private final EmbeddingJobJpaRepository jobRepository;
    private final EmbeddingStatusJpaRepository statusRepository;

    public JpaEmbeddingJobStoreAdapter(
            EmbeddingJobJpaRepository jobRepository,
            EmbeddingStatusJpaRepository statusRepository) {
        this.jobRepository = jobRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    public UUID createJob(
            UUID documentId,
            int totalChunks,
            String embeddingModel,
            int embeddingDimension,
            String correlationId) {
        Instant now = Instant.now();
        EmbeddingJobEntity job = new EmbeddingJobEntity();
        job.setId(UUID.randomUUID());
        job.setDocumentId(documentId);
        job.setTotalChunks(totalChunks);
        job.setCompletedChunks(0);
        job.setFailedChunks(0);
        job.setEmbeddingModel(embeddingModel);
        job.setEmbeddingDimension(embeddingDimension);
        job.setStatus(EmbeddingStatus.RECEIVED);
        job.setCorrelationId(correlationId);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        return jobRepository.save(job).getId();
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
        Instant now = Instant.now();
        EmbeddingStatusEntity entity =
                statusRepository
                        .findFirstByChunkIdAndEmbeddingModelAndChecksum(chunkId, model, checksum)
                        .orElseGet(
                                () -> {
                                    EmbeddingStatusEntity created = new EmbeddingStatusEntity();
                                    created.setId(UUID.randomUUID());
                                    created.setCreatedAt(now);
                                    created.setAttemptCount(0);
                                    return created;
                                });
        entity.setJobId(jobId);
        entity.setDocumentId(documentId);
        entity.setChunkId(chunkId);
        entity.setEmbeddingId(embeddingId);
        entity.setEmbeddingModel(model);
        entity.setEmbeddingDimension(dimension);
        entity.setChecksum(checksum);
        entity.setStatus(status);
        entity.setAttemptCount(entity.getAttemptCount() == null ? 1 : entity.getAttemptCount() + 1);
        entity.setUpdatedAt(now);
        if (status == EmbeddingStatus.COMPLETED || status == EmbeddingStatus.READY) {
            entity.setCompletedAt(now);
        }
        statusRepository.save(entity);
    }

    @Override
    public void completeJob(UUID jobId, EmbeddingStatus status) {
        jobRepository
                .findById(jobId)
                .ifPresent(
                        job -> {
                            job.setCompletedChunks(
                                    (int)
                                            statusRepository.countByDocumentIdAndStatus(
                                                    job.getDocumentId(),
                                                    EmbeddingStatus.COMPLETED));
                            job.setFailedChunks(
                                    (int)
                                            statusRepository.countByDocumentIdAndStatus(
                                                    job.getDocumentId(), EmbeddingStatus.FAILED));
                            job.setStatus(status);
                            job.setUpdatedAt(Instant.now());
                            job.setCompletedAt(Instant.now());
                            jobRepository.save(job);
                        });
    }

    @Override
    public void failJob(UUID jobId, String failureReason) {
        jobRepository
                .findById(jobId)
                .ifPresent(
                        job -> {
                            job.setFailedChunks(job.getTotalChunks());
                            job.setStatus(EmbeddingStatus.FAILED);
                            job.setUpdatedAt(Instant.now());
                            job.setCompletedAt(Instant.now());
                            jobRepository.save(job);
                        });
    }

    @Override
    public Optional<EmbeddingResponse> findByChunkId(UUID chunkId) {
        return statusRepository.findFirstByChunkId(chunkId).map(this::toEmbeddingResponse);
    }

    @Override
    public Optional<DocumentEmbeddingResponse> findLatestByDocumentId(UUID documentId) {
        return jobRepository
                .findFirstByDocumentIdOrderByCreatedAtDesc(documentId)
                .map(
                        job ->
                                new DocumentEmbeddingResponse(
                                        job.getId(),
                                        job.getDocumentId(),
                                        job.getTotalChunks(),
                                        null,
                                        job.getEmbeddingModel(),
                                        job.getEmbeddingDimension(),
                                        job.getStatus(),
                                        job.getCreatedAt()));
    }

    @Override
    public long countByDocumentIdAndStatus(UUID documentId, EmbeddingStatus status) {
        return statusRepository.countByDocumentIdAndStatus(documentId, status);
    }

    @Override
    public long countByDocumentId(UUID documentId) {
        return statusRepository.countByDocumentId(documentId);
    }

    private EmbeddingResponse toEmbeddingResponse(EmbeddingStatusEntity entity) {
        return new EmbeddingResponse(
                entity.getJobId(),
                entity.getDocumentId(),
                entity.getChunkId(),
                entity.getEmbeddingId(),
                entity.getEmbeddingModel(),
                entity.getEmbeddingDimension(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
