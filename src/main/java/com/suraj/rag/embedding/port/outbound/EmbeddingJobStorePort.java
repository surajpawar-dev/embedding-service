package com.suraj.rag.embedding.port.outbound;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import com.suraj.rag.embedding.dto.DocumentEmbeddingResponse;
import com.suraj.rag.embedding.dto.EmbeddingResponse;
import java.util.Optional;
import java.util.UUID;

public interface EmbeddingJobStorePort {
    UUID createJob(
            UUID documentId,
            int totalChunks,
            String embeddingModel,
            int embeddingDimension,
            String correlationId);

    void markChunk(
            UUID jobId,
            UUID documentId,
            UUID chunkId,
            UUID embeddingId,
            String model,
            int dimension,
            String checksum,
            EmbeddingStatus status);

    void completeJob(UUID jobId, EmbeddingStatus status);

    void failJob(UUID jobId, String failureReason);

    Optional<EmbeddingResponse> findByChunkId(UUID chunkId);

    Optional<DocumentEmbeddingResponse> findLatestByDocumentId(UUID documentId);

    long countByDocumentIdAndStatus(UUID documentId, EmbeddingStatus status);

    long countByDocumentId(UUID documentId);
}
