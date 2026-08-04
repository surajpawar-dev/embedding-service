package com.suraj.embeddingservice.dto;

import com.suraj.embeddingservice.domain.EmbeddingStatus;
import java.util.UUID;

public record StatusResponse(
        UUID documentId,
        long totalChunks,
        long completedChunks,
        long failedChunks,
        EmbeddingStatus status
) {
}
