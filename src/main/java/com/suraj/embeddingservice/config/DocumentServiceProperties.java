package com.suraj.embeddingservice.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.document-service")
public record DocumentServiceProperties(
        @NotBlank String baseUrl,
        @Min(1) int pageSize,
        Duration requestTimeout
) {
}
