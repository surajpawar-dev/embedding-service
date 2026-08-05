package com.suraj.rag.embedding.dto;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import java.util.UUID;

public record StatusResponse(
        UUID documentId,
        long totalChunks,
        long completedChunks,
        long failedChunks,
        EmbeddingStatus status
) {
}
