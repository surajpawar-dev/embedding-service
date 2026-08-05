package com.suraj.rag.embedding.port.inbound;

import com.suraj.rag.embedding.dto.BatchEmbeddingRequest;
import com.suraj.rag.embedding.dto.EmbeddingResponse;
import java.util.List;

public interface CreateBatchEmbeddingsUseCase {
    List<EmbeddingResponse> createBatch(BatchEmbeddingRequest request);
}
