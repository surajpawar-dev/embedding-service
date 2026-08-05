package com.suraj.rag.embedding.adapter.outbound.persistence;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import com.suraj.rag.embedding.domain.FailureStage;
import com.suraj.rag.embedding.entity.EmbeddingAuditEntity;
import com.suraj.rag.embedding.entity.FailureLogEntity;
import com.suraj.rag.embedding.entity.RetryLogEntity;
import com.suraj.rag.embedding.port.outbound.OperationalLogPort;
import com.suraj.rag.embedding.repository.EmbeddingAuditJpaRepository;
import com.suraj.rag.embedding.repository.FailureLogJpaRepository;
import com.suraj.rag.embedding.repository.RetryLogJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "job-store", name = "mode", havingValue = "jpa")
public class JpaOperationalLogAdapter implements OperationalLogPort {

    private final EmbeddingAuditJpaRepository auditRepository;
    private final FailureLogJpaRepository failureRepository;
    private final RetryLogJpaRepository retryRepository;

    public JpaOperationalLogAdapter(
            EmbeddingAuditJpaRepository auditRepository,
            FailureLogJpaRepository failureRepository,
            RetryLogJpaRepository retryRepository) {
        this.auditRepository = auditRepository;
        this.failureRepository = failureRepository;
        this.retryRepository = retryRepository;
    }

    @Override
    public void audit(
            UUID jobId,
            UUID documentId,
            UUID chunkId,
            String action,
            EmbeddingStatus before,
            EmbeddingStatus after,
            String details,
            String correlationId) {
        EmbeddingAuditEntity entity = new EmbeddingAuditEntity();
        entity.setId(UUID.randomUUID());
        entity.setJobId(jobId);
        entity.setDocumentId(documentId);
        entity.setChunkId(chunkId);
        entity.setAction(action);
        entity.setStatusBefore(before == null ? null : before.name());
        entity.setStatusAfter(after == null ? null : after.name());
        entity.setDetails(details);
        entity.setCorrelationId(correlationId);
        entity.setCreatedAt(Instant.now());
        auditRepository.save(entity);
    }

    @Override
    public void failure(
            UUID jobId,
            UUID documentId,
            UUID chunkId,
            FailureStage stage,
            String errorCode,
            String errorMessage,
            String payload,
            boolean permanent) {
        FailureLogEntity entity = new FailureLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setJobId(jobId);
        entity.setDocumentId(documentId);
        entity.setChunkId(chunkId);
        entity.setFailureStage(stage);
        entity.setErrorCode(errorCode);
        entity.setErrorMessage(errorMessage);
        entity.setPayload(payload);
        entity.setPermanent(permanent);
        entity.setCreatedAt(Instant.now());
        failureRepository.save(entity);
    }

    @Override
    public void retry(
            UUID jobId, UUID chunkId, int attemptNumber, String retryReason, String errorMessage) {
        RetryLogEntity entity = new RetryLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setJobId(jobId);
        entity.setChunkId(chunkId);
        entity.setAttemptNumber(attemptNumber);
        entity.setRetryReason(retryReason);
        entity.setErrorMessage(errorMessage);
        entity.setCreatedAt(Instant.now());
        retryRepository.save(entity);
    }
}
