package com.suraj.rag.embedding.adapter.outbound.opensearch;

import com.suraj.rag.embedding.domain.EmbeddingVector;
import com.suraj.rag.embedding.port.outbound.VectorStorePort;
import com.suraj.rag.embedding.port.outbound.VectorStorePort.ScoredEmbeddingVector;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "vector-store", name = "mode", havingValue = "in-memory")
public class InMemoryVectorStoreAdapter implements VectorStorePort {

    private final Map<UUID, EmbeddingVector> vectorsByChunkId = new ConcurrentHashMap<>();

    @Override
    public void upsertAll(List<EmbeddingVector> vectors) {
        vectors.forEach(vector -> vectorsByChunkId.put(vector.chunkId(), vector));
    }

    @Override
    public Optional<EmbeddingVector> findByChunkId(UUID chunkId) {
        return Optional.ofNullable(vectorsByChunkId.get(chunkId));
    }

    @Override
    public void deleteByChunkId(UUID chunkId) {
        vectorsByChunkId.remove(chunkId);
    }

    @Override
    public void deleteByDocumentIdAndModel(UUID documentId, String embeddingModel) {
        vectorsByChunkId.entrySet().removeIf(entry -> entry.getValue().documentId().equals(documentId)
                && entry.getValue().embeddingModel().equals(embeddingModel));
    }

    @Override
    public List<ScoredEmbeddingVector> search(float[] queryEmbedding, int topK, List<UUID> documentIds, String embeddingModel) {
        return vectorsByChunkId.values().stream()
                .filter(vector -> embeddingModel.equals(vector.embeddingModel()))
                .filter(vector -> documentIds == null || documentIds.isEmpty() || documentIds.contains(vector.documentId()))
                .map(vector -> new ScoredEmbeddingVector(vector, cosine(queryEmbedding, vector.embedding())))
                .sorted(Comparator.comparingDouble(ScoredEmbeddingVector::score).reversed())
                .limit(topK)
                .toList();
    }

    private double cosine(float[] left, float[] right) {
        double dot = 0.0;
        double leftMagnitude = 0.0;
        double rightMagnitude = 0.0;
        for (int i = 0; i < Math.min(left.length, right.length); i++) {
            dot += left[i] * right[i];
            leftMagnitude += left[i] * left[i];
            rightMagnitude += right[i] * right[i];
        }
        if (leftMagnitude == 0.0 || rightMagnitude == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
    }
}
