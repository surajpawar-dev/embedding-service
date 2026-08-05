package com.suraj.rag.embedding.port.inbound;

import com.suraj.rag.embedding.dto.DocumentEmbeddingResponse;
import com.suraj.rag.embedding.event.DocumentReadyEvent;
import java.util.UUID;

public interface StartDocumentEmbeddingUseCase {
    DocumentEmbeddingResponse start(UUID documentId);

    DocumentEmbeddingResponse handleDocumentReady(DocumentReadyEvent event);
}
