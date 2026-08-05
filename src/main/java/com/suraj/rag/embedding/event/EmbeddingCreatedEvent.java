package com.suraj.rag.embedding.event;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EmbeddingCreatedEvent(
        String eventType,
        UUID documentId,
        UUID embeddingJobId,
        List<UUID> embeddingIds,
        List<UUID> chunkIds,
        String embeddingModel,
        int embeddingDimension,
        EmbeddingStatus status,
        Instant createdAt,
        String correlationId) {}
