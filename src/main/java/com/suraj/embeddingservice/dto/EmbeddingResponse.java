package com.suraj.embeddingservice.dto;

import com.suraj.embeddingservice.domain.EmbeddingStatus;
import java.time.Instant;
import java.util.UUID;

public record EmbeddingResponse(
        UUID jobId,
        UUID documentId,
        UUID chunkId,
        UUID embeddingId,
        String embeddingModel,
        Integer embeddingDimension,
        EmbeddingStatus status,
        Instant createdAt
) {
}
