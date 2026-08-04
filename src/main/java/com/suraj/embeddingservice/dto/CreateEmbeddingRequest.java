package com.suraj.embeddingservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateEmbeddingRequest(
        @NotNull UUID documentId,
        @NotNull UUID chunkId,
        String embeddingModel
) {
}
