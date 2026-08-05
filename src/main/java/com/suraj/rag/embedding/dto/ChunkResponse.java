package com.suraj.rag.embedding.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ChunkResponse(
        UUID id,
        UUID documentId,
        Integer chunkOrder,
        String content,
        String checksum,
        Integer pageNumber,
        String section,
        String title,
        String language,
        String source,
        UUID parentChunkId,
        Map<String, Object> metadata,
        Instant createdAt
) {
}
