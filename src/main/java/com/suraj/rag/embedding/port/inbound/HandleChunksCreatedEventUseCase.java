package com.suraj.rag.embedding.port.inbound;

import com.suraj.rag.embedding.event.ChunksCreatedEvent;

public interface HandleChunksCreatedEventUseCase {
    void handle(ChunksCreatedEvent event);
}
