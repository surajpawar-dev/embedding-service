package com.suraj.embeddingservice.port.inbound;

import java.util.UUID;

public interface DeleteEmbeddingUseCase {
    void deleteByChunkId(UUID chunkId);
}
