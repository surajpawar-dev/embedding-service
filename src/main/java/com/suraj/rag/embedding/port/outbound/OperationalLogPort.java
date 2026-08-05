package com.suraj.rag.embedding.port.outbound;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import com.suraj.rag.embedding.domain.FailureStage;
import java.util.UUID;

public interface OperationalLogPort {
    void audit(UUID jobId, UUID documentId, UUID chunkId, String action, EmbeddingStatus before,
            EmbeddingStatus after, String details, String correlationId);

    void failure(UUID jobId, UUID documentId, UUID chunkId, FailureStage stage, String errorCode,
            String errorMessage, String payload, boolean permanent);

    void retry(UUID jobId, UUID chunkId, int attemptNumber, String retryReason, String errorMessage);
}
