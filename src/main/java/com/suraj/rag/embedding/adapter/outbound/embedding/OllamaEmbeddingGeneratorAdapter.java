package com.suraj.rag.embedding.adapter.outbound.embedding;

import com.suraj.rag.embedding.config.OllamaProperties;
import com.suraj.rag.embedding.exception.EmbeddingGenerationException;
import com.suraj.rag.embedding.exception.ErrorCode;
import com.suraj.rag.embedding.port.outbound.EmbeddingGeneratorPort;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(prefix = "embedding", name = "provider", havingValue = "ollama")
public class OllamaEmbeddingGeneratorAdapter implements EmbeddingGeneratorPort {

    private final RestTemplate ollamaRestTemplate;
    private final OllamaProperties properties;

    public OllamaEmbeddingGeneratorAdapter(RestTemplate ollamaRestTemplate, OllamaProperties properties) {
        this.ollamaRestTemplate = ollamaRestTemplate;
        this.properties = properties;
    }

    @Override
    @Retryable(retryFor = EmbeddingGenerationException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public List<float[]> embed(List<String> inputs, String model) {
        String resolvedModel = model == null || model.isBlank() ? properties.embeddingModel() : model;
        return inputs.stream()
                .map(input -> embedOne(input, resolvedModel))
                .toList();
    }

    private float[] embedOne(String input, String model) {
        try {
            OllamaEmbeddingResponse response = ollamaRestTemplate.postForObject(
                    "/api/embeddings",
                    new OllamaEmbeddingRequest(model, input),
                    OllamaEmbeddingResponse.class
            );
            if (response == null || response.embedding() == null || response.embedding().isEmpty()) {
                throw new EmbeddingGenerationException("Embedding provider returned an empty vector");
            }
            float[] vector = new float[response.embedding().size()];
            for (int i = 0; i < response.embedding().size(); i++) {
                vector[i] = response.embedding().get(i).floatValue();
            }
            return vector;
        } catch (RestClientException exception) {
            throw new EmbeddingGenerationException("Embedding provider request failed", exception);
        }
    }

    private record OllamaEmbeddingRequest(String model, String prompt) {
    }

    private record OllamaEmbeddingResponse(List<Double> embedding) {
    }
}
