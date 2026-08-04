package com.suraj.embeddingservice.adapter.inbound.sqs;

import com.suraj.embeddingservice.event.DocumentReadyEvent;
import com.suraj.embeddingservice.port.inbound.StartDocumentEmbeddingUseCase;
import org.springframework.stereotype.Component;

@Component
public class DocumentReadyListener {

    private final StartDocumentEmbeddingUseCase startDocumentEmbeddingUseCase;

    public DocumentReadyListener(StartDocumentEmbeddingUseCase startDocumentEmbeddingUseCase) {
        this.startDocumentEmbeddingUseCase = startDocumentEmbeddingUseCase;
    }

    public void onMessage(DocumentReadyEvent event) {
        startDocumentEmbeddingUseCase.handleDocumentReady(event);
    }
}
