package com.suraj.rag.embedding.dto;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
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
        Instant createdAt) {}
