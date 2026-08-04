package com.suraj.embeddingservice.port.inbound;

import com.suraj.embeddingservice.dto.DocumentEmbeddingResponse;
import com.suraj.embeddingservice.event.DocumentReadyEvent;
import java.util.UUID;

public interface StartDocumentEmbeddingUseCase {
    DocumentEmbeddingResponse start(UUID documentId);

    DocumentEmbeddingResponse handleDocumentReady(DocumentReadyEvent event);
}
