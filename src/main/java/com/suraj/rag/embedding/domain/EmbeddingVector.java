package com.suraj.rag.embedding.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EmbeddingVector(
        UUID embeddingId,
        UUID chunkId,
        UUID documentId,
        Integer chunkOrder,
        String content,
        float[] embedding,
        String embeddingModel,
        int embeddingDimension,
        Integer pageNumber,
        String section,
        String title,
        String language,
        String source,
        UUID parentChunkId,
        Map<String, Object> metadata,
        String documentChecksum,
        String checksum,
        Instant createdAt
) {
}
