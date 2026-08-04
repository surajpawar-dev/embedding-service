package com.suraj.embeddingservice.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record DocumentReadyEvent(
        @NotNull UUID documentId,
        @NotBlank String checksum,
        @Min(0) int chunkCount,
        @NotNull Instant readyAt
) {
}
