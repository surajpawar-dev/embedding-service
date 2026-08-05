package com.suraj.rag.embedding.port.outbound;

import com.suraj.rag.embedding.event.EmbeddingCreatedEvent;

public interface EventPublisherPort {
    void publish(EmbeddingCreatedEvent event);
}
