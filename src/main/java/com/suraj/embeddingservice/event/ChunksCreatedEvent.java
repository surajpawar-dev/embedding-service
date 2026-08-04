package com.suraj.embeddingservice.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChunksCreatedEvent(
        String eventType,
        UUID documentId,
        List<UUID> chunkIds,
        int totalChunks,
        Instant createdAt,
        String correlationId
) {
}
