package com.suraj.rag.embedding.port.inbound;

import com.suraj.rag.embedding.dto.CreateEmbeddingRequest;
import com.suraj.rag.embedding.dto.EmbeddingResponse;

public interface CreateEmbeddingUseCase {
    EmbeddingResponse create(CreateEmbeddingRequest request);
}
