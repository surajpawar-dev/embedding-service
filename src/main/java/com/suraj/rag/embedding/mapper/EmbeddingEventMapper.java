package com.suraj.rag.embedding.mapper;

import com.suraj.rag.embedding.common.EventTypes;
import com.suraj.rag.embedding.domain.EmbeddingStatus;
import com.suraj.rag.embedding.event.EmbeddingCreatedEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingEventMapper {

    public EmbeddingCreatedEvent toCreatedEvent(
            UUID documentId,
            UUID jobId,
            List<UUID> embeddingIds,
            List<UUID> chunkIds,
            String model,
            int dimension,
            EmbeddingStatus status,
            String correlationId
    ) {
        return new EmbeddingCreatedEvent(EventTypes.EMBEDDING_CREATED, documentId, jobId, embeddingIds, chunkIds,
                model, dimension, status, Instant.now(), correlationId);
    }
}
