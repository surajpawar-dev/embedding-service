package com.suraj.rag.embedding.service.impl;

import com.suraj.rag.embedding.config.EmbeddingProperties;
import com.suraj.rag.embedding.domain.EmbeddingStatus;
import com.suraj.rag.embedding.domain.EmbeddingStatusPolicy;
import com.suraj.rag.embedding.dto.EmbeddingResponse;
import com.suraj.rag.embedding.dto.EmbeddingSearchMatch;
import com.suraj.rag.embedding.dto.EmbeddingSearchRequest;
import com.suraj.rag.embedding.dto.EmbeddingSearchResponse;
import com.suraj.rag.embedding.dto.StatusResponse;
import com.suraj.rag.embedding.metrics.EmbeddingMetrics;
import com.suraj.rag.embedding.port.inbound.QueryEmbeddingUseCase;
import com.suraj.rag.embedding.port.inbound.SearchEmbeddingsUseCase;
import com.suraj.rag.embedding.port.outbound.EmbeddingGeneratorPort;
import com.suraj.rag.embedding.port.outbound.EmbeddingJobStorePort;
import com.suraj.rag.embedding.port.outbound.VectorStorePort;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmbeddingQueryServiceImpl implements QueryEmbeddingUseCase, SearchEmbeddingsUseCase {

    private final EmbeddingJobStorePort jobStorePort;
    private final EmbeddingStatusPolicy statusPolicy;
    private final EmbeddingGeneratorPort embeddingGeneratorPort;
    private final VectorStorePort vectorStorePort;
    private final EmbeddingProperties embeddingProperties;
    private final EmbeddingMetrics metrics;

    public EmbeddingQueryServiceImpl(
            EmbeddingJobStorePort jobStorePort,
            EmbeddingStatusPolicy statusPolicy,
            EmbeddingGeneratorPort embeddingGeneratorPort,
            VectorStorePort vectorStorePort,
            EmbeddingProperties embeddingProperties,
            EmbeddingMetrics metrics) {
        this.jobStorePort = jobStorePort;
        this.statusPolicy = statusPolicy;
        this.embeddingGeneratorPort = embeddingGeneratorPort;
        this.vectorStorePort = vectorStorePort;
        this.embeddingProperties = embeddingProperties;
        this.metrics = metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmbeddingResponse> findByChunkId(UUID chunkId) {
        return jobStorePort.findByChunkId(chunkId);
    }

    @Override
    @Transactional(readOnly = true)
    public StatusResponse getDocumentStatus(UUID documentId) {
        long total = jobStorePort.countByDocumentId(documentId);
        long completed =
                jobStorePort.countByDocumentIdAndStatus(documentId, EmbeddingStatus.COMPLETED);
        long failed = jobStorePort.countByDocumentIdAndStatus(documentId, EmbeddingStatus.FAILED);
        return new StatusResponse(
                documentId,
                total,
                completed,
                failed,
                statusPolicy.aggregate(total, completed, failed));
    }

    @Override
    @Transactional(readOnly = true)
    public EmbeddingSearchResponse search(EmbeddingSearchRequest request) {
        int topK = request.topK() == null ? 5 : request.topK();
        float[] queryEmbedding =
                metrics.recordOllama(
                                () ->
                                        embeddingGeneratorPort.embed(
                                                java.util.List.of(request.query()),
                                                embeddingProperties.model()))
                        .getFirst();
        return new EmbeddingSearchResponse(
                metrics
                        .recordVectorSearch(
                                () ->
                                        vectorStorePort.search(
                                                queryEmbedding,
                                                topK,
                                                request.documentIds(),
                                                embeddingProperties.model()))
                        .stream()
                        .map(
                                match ->
                                        new EmbeddingSearchMatch(
                                                match.vector().documentId(),
                                                match.vector().chunkId(),
                                                match.vector().chunkOrder(),
                                                match.vector().content(),
                                                match.score(),
                                                metadata(match)))
                        .toList());
    }

    private Map<String, Object> metadata(VectorStorePort.ScoredEmbeddingVector match) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        putIfPresent(metadata, "pageNumber", match.vector().pageNumber());
        putIfPresent(metadata, "source", match.vector().source());
        putIfPresent(metadata, "language", match.vector().language());
        putIfPresent(metadata, "embeddingModel", match.vector().embeddingModel());
        putIfPresent(metadata, "documentChecksum", match.vector().documentChecksum());
        putIfPresent(metadata, "chunkChecksum", match.vector().checksum());
        return metadata;
    }

    private void putIfPresent(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }
}
