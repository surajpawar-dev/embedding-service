package com.suraj.embeddingservice.controller;

import com.suraj.embeddingservice.common.ApiPaths;
import com.suraj.embeddingservice.dto.BatchEmbeddingRequest;
import com.suraj.embeddingservice.dto.CreateEmbeddingRequest;
import com.suraj.embeddingservice.dto.DocumentEmbeddingResponse;
import com.suraj.embeddingservice.dto.EmbeddingSearchRequest;
import com.suraj.embeddingservice.dto.EmbeddingSearchResponse;
import com.suraj.embeddingservice.dto.EmbeddingResponse;
import com.suraj.embeddingservice.dto.StatusResponse;
import com.suraj.embeddingservice.event.DocumentReadyEvent;
import com.suraj.embeddingservice.exception.NotFoundException;
import com.suraj.embeddingservice.port.inbound.CreateBatchEmbeddingsUseCase;
import com.suraj.embeddingservice.port.inbound.CreateEmbeddingUseCase;
import com.suraj.embeddingservice.port.inbound.DeleteEmbeddingUseCase;
import com.suraj.embeddingservice.port.inbound.QueryEmbeddingUseCase;
import com.suraj.embeddingservice.port.inbound.SearchEmbeddingsUseCase;
import com.suraj.embeddingservice.port.inbound.StartDocumentEmbeddingUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.EMBEDDINGS)
public class EmbeddingController {

    private final CreateEmbeddingUseCase createEmbeddingUseCase;
    private final CreateBatchEmbeddingsUseCase createBatchEmbeddingsUseCase;
    private final QueryEmbeddingUseCase queryEmbeddingUseCase;
    private final DeleteEmbeddingUseCase deleteEmbeddingUseCase;
    private final StartDocumentEmbeddingUseCase startDocumentEmbeddingUseCase;
    private final SearchEmbeddingsUseCase searchEmbeddingsUseCase;

    public EmbeddingController(
            CreateEmbeddingUseCase createEmbeddingUseCase,
            CreateBatchEmbeddingsUseCase createBatchEmbeddingsUseCase,
            QueryEmbeddingUseCase queryEmbeddingUseCase,
            DeleteEmbeddingUseCase deleteEmbeddingUseCase,
            StartDocumentEmbeddingUseCase startDocumentEmbeddingUseCase,
            SearchEmbeddingsUseCase searchEmbeddingsUseCase
    ) {
        this.createEmbeddingUseCase = createEmbeddingUseCase;
        this.createBatchEmbeddingsUseCase = createBatchEmbeddingsUseCase;
        this.queryEmbeddingUseCase = queryEmbeddingUseCase;
        this.deleteEmbeddingUseCase = deleteEmbeddingUseCase;
        this.startDocumentEmbeddingUseCase = startDocumentEmbeddingUseCase;
        this.searchEmbeddingsUseCase = searchEmbeddingsUseCase;
    }

    @PostMapping
    public ResponseEntity<EmbeddingResponse> create(@Valid @RequestBody CreateEmbeddingRequest request) {
        EmbeddingResponse response = createEmbeddingUseCase.create(request);
        return ResponseEntity.accepted()
                .location(URI.create(ApiPaths.EMBEDDINGS + "/" + response.chunkId()))
                .body(response);
    }

    @PostMapping(ApiPaths.EMBEDDINGS_BATCH)
    public ResponseEntity<List<EmbeddingResponse>> createBatch(@Valid @RequestBody BatchEmbeddingRequest request) {
        return ResponseEntity.accepted().body(createBatchEmbeddingsUseCase.createBatch(request));
    }

    @PostMapping(ApiPaths.EMBEDDINGS_DOCUMENTS)
    public ResponseEntity<DocumentEmbeddingResponse> startDocumentEmbedding(@PathVariable UUID documentId) {
        return ResponseEntity.accepted().body(startDocumentEmbeddingUseCase.start(documentId));
    }

    @PostMapping(ApiPaths.EMBEDDINGS_DOCUMENT_READY_EVENTS)
    public ResponseEntity<DocumentEmbeddingResponse> handleDocumentReady(@Valid @RequestBody DocumentReadyEvent event) {
        return ResponseEntity.accepted().body(startDocumentEmbeddingUseCase.handleDocumentReady(event));
    }

    @GetMapping(ApiPaths.EMBEDDINGS_DOCUMENT_STATUS)
    public ResponseEntity<StatusResponse> getDocumentStatus(@PathVariable UUID documentId) {
        return ResponseEntity.ok(queryEmbeddingUseCase.getDocumentStatus(documentId));
    }

    @PostMapping(ApiPaths.EMBEDDINGS_SEARCH)
    public ResponseEntity<EmbeddingSearchResponse> search(@Valid @RequestBody EmbeddingSearchRequest request) {
        return ResponseEntity.ok(searchEmbeddingsUseCase.search(request));
    }

    @GetMapping("/{chunkId}")
    public ResponseEntity<EmbeddingResponse> findByChunkId(@PathVariable UUID chunkId) {
        EmbeddingResponse response = queryEmbeddingUseCase.findByChunkId(chunkId)
                .orElseThrow(() -> new NotFoundException("Embedding not found for chunkId=" + chunkId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{chunkId}")
    public ResponseEntity<Void> deleteByChunkId(@PathVariable UUID chunkId) {
        deleteEmbeddingUseCase.deleteByChunkId(chunkId);
        return ResponseEntity.noContent().build();
    }
}
