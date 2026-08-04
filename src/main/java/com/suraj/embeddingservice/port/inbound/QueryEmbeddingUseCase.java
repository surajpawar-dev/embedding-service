package com.suraj.embeddingservice.port.inbound;

import com.suraj.embeddingservice.dto.EmbeddingResponse;
import com.suraj.embeddingservice.dto.StatusResponse;
import java.util.Optional;
import java.util.UUID;

public interface QueryEmbeddingUseCase {
    Optional<EmbeddingResponse> findByChunkId(UUID chunkId);

    StatusResponse getDocumentStatus(UUID documentId);
}
