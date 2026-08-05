package com.suraj.rag.embedding.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aws")
public record AwsSqsProperties(
        @NotBlank String region,
        Sqs sqs
) {
    public record Sqs(
            boolean listenerEnabled,
            String endpoint,
            String documentReadyQueueUrl,
            String chunksCreatedQueueUrl,
            String embeddingCreatedQueueUrl,
            String deadLetterQueueUrl,
            @Min(1) int maxMessages,
            @Min(0) int waitTimeSeconds,
            @Min(1) int visibilityTimeoutSeconds
    ) {
    }
}
