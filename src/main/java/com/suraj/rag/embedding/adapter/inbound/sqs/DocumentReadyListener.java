package com.suraj.rag.embedding.adapter.inbound.sqs;

import com.suraj.rag.embedding.event.DocumentReadyEvent;
import com.suraj.rag.embedding.port.inbound.StartDocumentEmbeddingUseCase;
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
