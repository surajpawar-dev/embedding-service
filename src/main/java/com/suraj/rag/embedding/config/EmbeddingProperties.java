package com.suraj.rag.embedding.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "embedding")
public record EmbeddingProperties(
        @NotBlank String provider,
        @NotBlank String model,
        @Min(1) int dimension,
        @Min(1) int batchSize,
        @Min(1) int workers,
        @Min(1) int queueCapacity,
        Duration timeout
) {
}
