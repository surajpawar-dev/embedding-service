package com.suraj.embeddingservice.port.outbound;

import com.suraj.embeddingservice.dto.ChunkResponse;
import java.util.List;
import java.util.UUID;

public interface ChunkClientPort {
    List<ChunkResponse> fetchChunks(UUID documentId, List<UUID> chunkIds);

    List<ChunkResponse> fetchAllChunks(UUID documentId);
}
