package com.suraj.embeddingservice.port.outbound;

import com.suraj.embeddingservice.domain.EmbeddingVector;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VectorStorePort {
    void upsertAll(List<EmbeddingVector> vectors);

    Optional<EmbeddingVector> findByChunkId(UUID chunkId);

    void deleteByChunkId(UUID chunkId);

    void deleteByDocumentIdAndModel(UUID documentId, String embeddingModel);

    List<ScoredEmbeddingVector> search(float[] queryEmbedding, int topK, List<UUID> documentIds, String embeddingModel);

    record ScoredEmbeddingVector(EmbeddingVector vector, double score) {
    }
}
