package com.suraj.rag.embedding.port.inbound;

import com.suraj.rag.embedding.dto.EmbeddingResponse;
import com.suraj.rag.embedding.dto.StatusResponse;
import java.util.Optional;
import java.util.UUID;

public interface QueryEmbeddingUseCase {
    Optional<EmbeddingResponse> findByChunkId(UUID chunkId);

    StatusResponse getDocumentStatus(UUID documentId);
}
