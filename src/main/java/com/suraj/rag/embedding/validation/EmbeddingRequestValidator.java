package com.suraj.rag.embedding.validation;

import com.suraj.rag.embedding.dto.BatchEmbeddingRequest;
import com.suraj.rag.embedding.dto.ChunkResponse;
import com.suraj.rag.embedding.exception.ErrorCode;
import com.suraj.rag.embedding.exception.ErrorMessage;
import com.suraj.rag.embedding.exception.InvalidEmbeddingRequestException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingRequestValidator {

    public void validateBatchRequest(BatchEmbeddingRequest request) {
        Set<UUID> uniqueChunkIds = new HashSet<>(request.chunkIds());
        if (uniqueChunkIds.size() != request.chunkIds().size()) {
            throw new InvalidEmbeddingRequestException(
                    ErrorCode.DUPLICATE_CHUNK_IDS, ErrorMessage.DUPLICATE_CHUNK_IDS);
        }
    }

    public void validateFetchedChunks(BatchEmbeddingRequest request, List<ChunkResponse> chunks) {
        if (chunks.size() != request.chunkIds().size()) {
            throw new InvalidEmbeddingRequestException(
                    ErrorCode.CHUNK_FETCH_FAILED, ErrorMessage.INCOMPLETE_CHUNK_SET);
        }
        chunks.forEach(this::validateChunk);
    }

    private void validateChunk(ChunkResponse chunk) {
        if (chunk.content() == null || chunk.content().isBlank()) {
            throw new InvalidEmbeddingRequestException(
                    ErrorCode.CHUNK_CONTENT_EMPTY, ErrorMessage.EMPTY_CHUNK_CONTENT);
        }
        if (chunk.checksum() == null || chunk.checksum().isBlank()) {
            throw new InvalidEmbeddingRequestException(
                    ErrorCode.CHECKSUM_VALIDATION_FAILED, ErrorMessage.EMPTY_CHUNK_CHECKSUM);
        }
    }
}
