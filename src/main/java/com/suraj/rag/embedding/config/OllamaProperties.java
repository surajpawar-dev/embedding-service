package com.suraj.rag.embedding.config;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.ollama")
public record OllamaProperties(
        @NotBlank String baseUrl, @NotBlank String embeddingModel, Duration requestTimeout) {}
