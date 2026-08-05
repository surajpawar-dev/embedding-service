package com.suraj.rag.embedding.adapter.outbound.embedding;

import com.suraj.rag.embedding.config.EmbeddingProperties;
import com.suraj.rag.embedding.port.outbound.EmbeddingGeneratorPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "embedding", name = "provider", havingValue = "mock")
public class MockEmbeddingGeneratorAdapter implements EmbeddingGeneratorPort {

    private final EmbeddingProperties embeddingProperties;

    public MockEmbeddingGeneratorAdapter(EmbeddingProperties embeddingProperties) {
        this.embeddingProperties = embeddingProperties;
    }

    @Override
    public List<float[]> embed(List<String> inputs, String model) {
        return inputs.stream().map(this::deterministicVector).toList();
    }

    private float[] deterministicVector(String input) {
        byte[] hash = sha256(input == null ? "" : input);
        float[] vector = new float[embeddingProperties.dimension()];
        for (int i = 0; i < vector.length; i++) {
            int unsigned = hash[i % hash.length] & 0xff;
            vector[i] = (unsigned / 255.0f) - 0.5f;
        }
        return vector;
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
