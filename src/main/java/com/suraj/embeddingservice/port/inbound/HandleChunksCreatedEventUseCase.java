package com.suraj.embeddingservice.port.inbound;

import com.suraj.embeddingservice.event.ChunksCreatedEvent;

public interface HandleChunksCreatedEventUseCase {
    void handle(ChunksCreatedEvent event);
}
