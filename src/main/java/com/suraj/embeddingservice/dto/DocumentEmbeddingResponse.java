package com.suraj.embeddingservice.dto;

import com.suraj.embeddingservice.domain.EmbeddingStatus;
import java.time.Instant;
import java.util.UUID;

public record DocumentEmbeddingResponse(
        UUID jobId,
        UUID documentId,
        int acceptedChunks,
        String documentChecksum,
        String embeddingModel,
        int embeddingDimension,
        EmbeddingStatus status,
        Instant createdAt
) {
}
