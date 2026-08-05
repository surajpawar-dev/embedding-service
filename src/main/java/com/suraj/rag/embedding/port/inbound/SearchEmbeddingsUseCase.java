package com.suraj.rag.embedding.port.inbound;

import com.suraj.rag.embedding.dto.EmbeddingSearchRequest;
import com.suraj.rag.embedding.dto.EmbeddingSearchResponse;

public interface SearchEmbeddingsUseCase {
    EmbeddingSearchResponse search(EmbeddingSearchRequest request);
}
