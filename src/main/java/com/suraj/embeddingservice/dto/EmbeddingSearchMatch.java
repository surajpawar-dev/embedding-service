package com.suraj.embeddingservice.dto;

import java.util.Map;
import java.util.UUID;

public record EmbeddingSearchMatch(
        UUID documentId,
        UUID chunkId,
        Integer chunkOrder,
        String content,
        double score,
        Map<String, Object> metadata
) {
}
