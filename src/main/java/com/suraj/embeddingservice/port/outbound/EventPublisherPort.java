package com.suraj.embeddingservice.port.outbound;

import com.suraj.embeddingservice.event.EmbeddingCreatedEvent;

public interface EventPublisherPort {
    void publish(EmbeddingCreatedEvent event);
}
