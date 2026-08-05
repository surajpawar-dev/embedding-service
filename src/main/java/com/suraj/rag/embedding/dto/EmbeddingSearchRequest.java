package com.suraj.rag.embedding.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record EmbeddingSearchRequest(
        @NotBlank String query, @Min(1) @Max(50) Integer topK, List<UUID> documentIds) {}
