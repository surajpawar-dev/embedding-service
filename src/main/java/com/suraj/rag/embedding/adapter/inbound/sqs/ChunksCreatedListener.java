package com.suraj.rag.embedding.adapter.inbound.sqs;

import com.suraj.rag.embedding.event.ChunksCreatedEvent;
import com.suraj.rag.embedding.port.inbound.HandleChunksCreatedEventUseCase;
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
