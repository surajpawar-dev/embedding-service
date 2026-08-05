package com.suraj.rag.embedding.adapter.outbound;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import com.suraj.rag.embedding.domain.FailureStage;
import com.suraj.rag.embedding.port.outbound.OperationalLogPort;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "job-store", name = "mode", havingValue = "in-memory")
public class NoopOperationalLogAdapter implements OperationalLogPort {

    @Override
    public void audit(UUID jobId, UUID documentId, UUID chunkId, String action, EmbeddingStatus before,
            EmbeddingStatus after, String details, String correlationId) {
    }

    @Override
    public void failure(UUID jobId, UUID documentId, UUID chunkId, FailureStage stage, String errorCode,
            String errorMessage, String payload, boolean permanent) {
    }

    @Override
    public void retry(UUID jobId, UUID chunkId, int attemptNumber, String retryReason, String errorMessage) {
    }
}
