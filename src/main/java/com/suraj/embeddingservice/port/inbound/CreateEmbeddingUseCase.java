package com.suraj.embeddingservice.port.inbound;

import com.suraj.embeddingservice.dto.CreateEmbeddingRequest;
import com.suraj.embeddingservice.dto.EmbeddingResponse;

public interface CreateEmbeddingUseCase {
    EmbeddingResponse create(CreateEmbeddingRequest request);
}
