package com.suraj.rag.embedding.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record BatchEmbeddingRequest(
        @NotNull UUID documentId,
        @NotEmpty @Size(max = 1000) List<UUID> chunkIds,
        String embeddingModel
) {
}
