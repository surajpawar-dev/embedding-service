package com.suraj.rag.embedding.mapper;

import com.suraj.rag.embedding.domain.EmbeddingVector;
import com.suraj.rag.embedding.dto.ChunkResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingVectorMapper {

    public EmbeddingVector toVector(ChunkResponse chunk, float[] embedding, UUID embeddingId, String model, int dimension) {
        return toVector(chunk, embedding, embeddingId, model, dimension, null);
    }

    public EmbeddingVector toVector(ChunkResponse chunk, float[] embedding, UUID embeddingId, String model, int dimension,
            String documentChecksum) {
        return new EmbeddingVector(
                embeddingId,
                chunk.id(),
                chunk.documentId(),
                chunk.chunkOrder(),
                chunk.content(),
                embedding,
                model,
                dimension,
                chunk.pageNumber(),
                chunk.section(),
                chunk.title(),
                chunk.language(),
                chunk.source(),
                chunk.parentChunkId(),
                chunk.metadata(),
                documentChecksum,
                chunk.checksum(),
                Instant.now()
        );
    }
}
