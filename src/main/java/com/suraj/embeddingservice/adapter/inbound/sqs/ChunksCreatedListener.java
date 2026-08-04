package com.suraj.embeddingservice.adapter.inbound.sqs;

import com.suraj.embeddingservice.event.ChunksCreatedEvent;
import com.suraj.embeddingservice.port.inbound.HandleChunksCreatedEventUseCase;
import org.springframework.stereotype.Component;

@Component
public class ChunksCreatedListener {

    private final HandleChunksCreatedEventUseCase handleChunksCreatedEventUseCase;

    public ChunksCreatedListener(HandleChunksCreatedEventUseCase handleChunksCreatedEventUseCase) {
        this.handleChunksCreatedEventUseCase = handleChunksCreatedEventUseCase;
    }

    public void onMessage(ChunksCreatedEvent event) {
        handleChunksCreatedEventUseCase.handle(event);
    }
}
