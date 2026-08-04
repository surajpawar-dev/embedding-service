package com.suraj.embeddingservice.port.inbound;

import com.suraj.embeddingservice.dto.EmbeddingSearchRequest;
import com.suraj.embeddingservice.dto.EmbeddingSearchResponse;

public interface SearchEmbeddingsUseCase {
    EmbeddingSearchResponse search(EmbeddingSearchRequest request);
}
