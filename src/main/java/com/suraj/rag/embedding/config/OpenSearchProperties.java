package com.suraj.rag.embedding.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "opensearch")
public record OpenSearchProperties(
        @NotBlank String endpoint,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String readAlias,
        @NotBlank String writeAlias,
        @Valid Bulk bulk,
        @Valid Bootstrap bootstrap
) {
    public record Bulk(@Min(1) int batchSize, @Min(1) int maxPayloadMb) {
    }

    public record Bootstrap(boolean enabled, @NotBlank String indexName) {
    }
}
