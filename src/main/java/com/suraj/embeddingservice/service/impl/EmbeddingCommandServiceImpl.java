package com.suraj.embeddingservice.service.impl;

import com.suraj.embeddingservice.config.EmbeddingProperties;
import com.suraj.embeddingservice.domain.EmbeddingStatus;
import com.suraj.embeddingservice.domain.EmbeddingVector;
import com.suraj.embeddingservice.domain.FailureStage;
import com.suraj.embeddingservice.dto.BatchEmbeddingRequest;
import com.suraj.embeddingservice.dto.ChunkResponse;
import com.suraj.embeddingservice.dto.CreateEmbeddingRequest;
import com.suraj.embeddingservice.dto.DocumentEmbeddingResponse;
import com.suraj.embeddingservice.dto.EmbeddingResponse;
import com.suraj.embeddingservice.event.ChunksCreatedEvent;
import com.suraj.embeddingservice.event.DocumentReadyEvent;
import com.suraj.embeddingservice.mapper.EmbeddingEventMapper;
import com.suraj.embeddingservice.mapper.EmbeddingVectorMapper;
import com.suraj.embeddingservice.metrics.EmbeddingMetrics;
import com.suraj.embeddingservice.port.inbound.CreateBatchEmbeddingsUseCase;
import com.suraj.embeddingservice.port.inbound.CreateEmbeddingUseCase;
import com.suraj.embeddingservice.port.inbound.DeleteEmbeddingUseCase;
import com.suraj.embeddingservice.port.inbound.HandleChunksCreatedEventUseCase;
import com.suraj.embeddingservice.port.inbound.StartDocumentEmbeddingUseCase;
import com.suraj.embeddingservice.port.outbound.ChunkClientPort;
import com.suraj.embeddingservice.port.outbound.EmbeddingGeneratorPort;
import com.suraj.embeddingservice.port.outbound.EmbeddingJobStorePort;
import com.suraj.embeddingservice.port.outbound.EventPublisherPort;
import com.suraj.embeddingservice.port.outbound.OperationalLogPort;
import com.suraj.embeddingservice.port.outbound.VectorStorePort;
import com.suraj.embeddingservice.util.CorrelationIdProvider;
import com.suraj.embeddingservice.validation.EmbeddingRequestValidator;
import com.suraj.embeddingservice.validation.EmbeddingVectorValidator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmbeddingCommandServiceImpl implements CreateEmbeddingUseCase, CreateBatchEmbeddingsUseCase,
        HandleChunksCreatedEventUseCase, DeleteEmbeddingUseCase, StartDocumentEmbeddingUseCase {

    private final EmbeddingProperties embeddingProperties;
    private final ChunkClientPort chunkClientPort;
    private final EmbeddingGeneratorPort embeddingGeneratorPort;
    private final VectorStorePort vectorStorePort;
    private final EmbeddingJobStorePort jobStorePort;
    private final EventPublisherPort eventPublisherPort;
    private final EmbeddingRequestValidator requestValidator;
    private final EmbeddingVectorValidator vectorValidator;
    private final EmbeddingVectorMapper vectorMapper;
    private final EmbeddingEventMapper eventMapper;
    private final CorrelationIdProvider correlationIdProvider;
    private final OperationalLogPort operationalLogPort;
    private final EmbeddingMetrics metrics;

    public EmbeddingCommandServiceImpl(
            EmbeddingProperties embeddingProperties,
            ChunkClientPort chunkClientPort,
            EmbeddingGeneratorPort embeddingGeneratorPort,
            VectorStorePort vectorStorePort,
            EmbeddingJobStorePort jobStorePort,
            EventPublisherPort eventPublisherPort,
            EmbeddingRequestValidator requestValidator,
            EmbeddingVectorValidator vectorValidator,
            EmbeddingVectorMapper vectorMapper,
            EmbeddingEventMapper eventMapper,
            CorrelationIdProvider correlationIdProvider,
            OperationalLogPort operationalLogPort,
            EmbeddingMetrics metrics
    ) {
        this.embeddingProperties = embeddingProperties;
        this.chunkClientPort = chunkClientPort;
        this.embeddingGeneratorPort = embeddingGeneratorPort;
        this.vectorStorePort = vectorStorePort;
        this.jobStorePort = jobStorePort;
        this.eventPublisherPort = eventPublisherPort;
        this.requestValidator = requestValidator;
        this.vectorValidator = vectorValidator;
        this.vectorMapper = vectorMapper;
        this.eventMapper = eventMapper;
        this.correlationIdProvider = correlationIdProvider;
        this.operationalLogPort = operationalLogPort;
        this.metrics = metrics;
    }

    @Override
    @Transactional
    public EmbeddingResponse create(CreateEmbeddingRequest request) {
        return createBatch(new BatchEmbeddingRequest(request.documentId(), List.of(request.chunkId()), request.embeddingModel()))
                .getFirst();
    }

    @Override
    @Transactional
    public List<EmbeddingResponse> createBatch(BatchEmbeddingRequest request) {
        requestValidator.validateBatchRequest(request);
        String model = resolveModel(request.embeddingModel());
        List<ChunkResponse> chunks = metrics.recordChunkFetch(() -> chunkClientPort.fetchChunks(request.documentId(),
                request.chunkIds()));
        requestValidator.validateFetchedChunks(request, chunks);
        return processChunks(request.documentId(), chunks, model, null).responses();
    }

    @Override
    @Transactional
    public DocumentEmbeddingResponse start(UUID documentId) {
        return embedDocument(documentId, null);
    }

    @Override
    @Transactional
    public DocumentEmbeddingResponse handleDocumentReady(DocumentReadyEvent event) {
        return embedDocument(event.documentId(), event.checksum());
    }

    private DocumentEmbeddingResponse embedDocument(UUID documentId, String documentChecksum) {
        String model = resolveModel(null);
        List<ChunkResponse> chunks = metrics.recordChunkFetch(() -> chunkClientPort.fetchAllChunks(documentId));
        requestValidator.validateFetchedChunks(new BatchEmbeddingRequest(documentId,
                chunks.stream().map(ChunkResponse::id).toList(), model), chunks);
        vectorStorePort.deleteByDocumentIdAndModel(documentId, model);
        ProcessingResult result = processChunks(documentId, chunks, model, documentChecksum);
        return new DocumentEmbeddingResponse(result.jobId(), documentId, result.responses().size(), documentChecksum, model,
                embeddingProperties.dimension(), result.status(), Instant.now());
    }

    private ProcessingResult processChunks(UUID documentId, List<ChunkResponse> chunks, String model,
            String documentChecksum) {
        String correlationId = correlationIdProvider.currentOrNew();
        UUID jobId = jobStorePort.createJob(documentId, chunks.size(), model,
                embeddingProperties.dimension(), correlationId);
        operationalLogPort.audit(jobId, documentId, null, "JOB_RECEIVED", null, EmbeddingStatus.RECEIVED,
                "Embedding job accepted", correlationId);
        try {
            List<float[]> embeddings = metrics.recordOllama(() -> embeddingGeneratorPort.embed(
                    chunks.stream().map(ChunkResponse::content).toList(), model));
            vectorValidator.validateEmbeddingResult(chunks.size(), embeddings, embeddingProperties.dimension());
            List<EmbeddingVector> vectors = new ArrayList<>(chunks.size());
            List<EmbeddingResponse> responses = new ArrayList<>(chunks.size());
            List<UUID> embeddingIds = new ArrayList<>(chunks.size());

            for (int i = 0; i < chunks.size(); i++) {
                ChunkResponse chunk = chunks.get(i);
                UUID embeddingId = UUID.randomUUID();
                embeddingIds.add(embeddingId);
                vectors.add(vectorMapper.toVector(chunk, embeddings.get(i), embeddingId, model, embeddingProperties.dimension(),
                        documentChecksum));
                jobStorePort.markChunk(jobId, chunk.documentId(), chunk.id(), embeddingId, model,
                        embeddingProperties.dimension(), chunk.checksum(), EmbeddingStatus.COMPLETED);
                responses.add(new EmbeddingResponse(jobId, chunk.documentId(), chunk.id(), embeddingId, model,
                        embeddingProperties.dimension(), EmbeddingStatus.COMPLETED, Instant.now()));
            }

            metrics.recordVectorWrite(() -> vectorStorePort.upsertAll(vectors));
            jobStorePort.completeJob(jobId, EmbeddingStatus.READY);
            metrics.incrementProcessed(chunks.size());
            metrics.incrementJobsCompleted();
            operationalLogPort.audit(jobId, documentId, null, "JOB_READY", EmbeddingStatus.RECEIVED,
                    EmbeddingStatus.READY, "Embedding job completed", correlationId);
            eventPublisherPort.publish(eventMapper.toCreatedEvent(documentId, jobId, embeddingIds,
                    chunks.stream().map(ChunkResponse::id).toList(), model, embeddingProperties.dimension(),
                    EmbeddingStatus.READY, correlationId));
            return new ProcessingResult(jobId, responses, EmbeddingStatus.READY);
        } catch (RuntimeException exception) {
            jobStorePort.failJob(jobId, exception.getMessage());
            metrics.incrementFailed(Math.max(1, chunks.size()));
            metrics.incrementJobsFailed();
            operationalLogPort.failure(jobId, documentId, null, failureStage(exception), exception.getClass().getSimpleName(),
                    exception.getMessage(), null, false);
            operationalLogPort.audit(jobId, documentId, null, "JOB_FAILED", EmbeddingStatus.RECEIVED,
                    EmbeddingStatus.FAILED, exception.getMessage(), correlationId);
            throw exception;
        }
    }

    @Override
    @Transactional
    public void handle(ChunksCreatedEvent event) {
        createBatch(new BatchEmbeddingRequest(event.documentId(), event.chunkIds(), null));
    }

    @Override
    @Transactional
    public void deleteByChunkId(UUID chunkId) {
        vectorStorePort.deleteByChunkId(chunkId);
    }

    private String resolveModel(String requestedModel) {
        return requestedModel == null || requestedModel.isBlank() ? embeddingProperties.model() : requestedModel;
    }

    private FailureStage failureStage(RuntimeException exception) {
        String packageName = exception.getClass().getPackageName();
        String className = exception.getClass().getSimpleName();
        if (className.contains("Chunk")) {
            return FailureStage.FETCH_CHUNK;
        }
        if (className.contains("Embedding")) {
            return FailureStage.EMBEDDING;
        }
        if (className.contains("Vector")) {
            return FailureStage.OPENSEARCH;
        }
        if (packageName.contains("sqs") || className.contains("Sqs")) {
            return FailureStage.PUBLISH_EVENT;
        }
        return FailureStage.UNKNOWN;
    }

    private record ProcessingResult(UUID jobId, List<EmbeddingResponse> responses, EmbeddingStatus status) {
    }
}
