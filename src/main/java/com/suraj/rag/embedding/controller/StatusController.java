package com.suraj.rag.embedding.controller;

import com.suraj.rag.embedding.common.ApiPaths;
import com.suraj.rag.embedding.dto.StatusResponse;
import com.suraj.rag.embedding.port.inbound.QueryEmbeddingUseCase;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.STATUS)
public class StatusController {

    private final QueryEmbeddingUseCase queryEmbeddingUseCase;

    public StatusController(QueryEmbeddingUseCase queryEmbeddingUseCase) {
        this.queryEmbeddingUseCase = queryEmbeddingUseCase;
    }

    @GetMapping("/{documentId}")
    public StatusResponse getDocumentStatus(@PathVariable UUID documentId) {
        return queryEmbeddingUseCase.getDocumentStatus(documentId);
    }
}
