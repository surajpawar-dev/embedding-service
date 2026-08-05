package com.suraj.rag.embedding.port.inbound;

import java.util.UUID;

public interface DeleteEmbeddingUseCase {
    void deleteByChunkId(UUID chunkId);
}
