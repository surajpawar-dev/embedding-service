package com.suraj.embeddingservice.port.inbound;

import com.suraj.embeddingservice.dto.BatchEmbeddingRequest;
import com.suraj.embeddingservice.dto.EmbeddingResponse;
import java.util.List;

public interface CreateBatchEmbeddingsUseCase {
    List<EmbeddingResponse> createBatch(BatchEmbeddingRequest request);
}
