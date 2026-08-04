package com.suraj.embeddingservice.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.suraj.embeddingservice.dto.BatchEmbeddingRequest;
import com.suraj.embeddingservice.dto.ChunkResponse;
import com.suraj.embeddingservice.exception.ErrorCode;
import com.suraj.embeddingservice.exception.InvalidEmbeddingRequestException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmbeddingRequestValidatorTest {

    private final EmbeddingRequestValidator validator = new EmbeddingRequestValidator();

    @Test
    void rejectsDuplicateChunkIds() {
        UUID documentId = UUID.randomUUID();
        UUID chunkId = UUID.randomUUID();

        assertThatThrownBy(() -> validator.validateBatchRequest(
                new BatchEmbeddingRequest(documentId, List.of(chunkId, chunkId), null)))
                .isInstanceOfSatisfying(InvalidEmbeddingRequestException.class,
                        exception -> org.assertj.core.api.Assertions.assertThat(exception.errorCode())
                                .isEqualTo(ErrorCode.DUPLICATE_CHUNK_IDS));
    }

    @Test
    void rejectsBlankChunkChecksum() {
        UUID documentId = UUID.randomUUID();
        UUID chunkId = UUID.randomUUID();
        BatchEmbeddingRequest request = new BatchEmbeddingRequest(documentId, List.of(chunkId), null);
        ChunkResponse chunk = new ChunkResponse(chunkId, documentId, 0, "content", " ", 1, null,
                null, "en", "test", null, Map.of(), Instant.now());

        assertThatThrownBy(() -> validator.validateFetchedChunks(request, List.of(chunk)))
                .isInstanceOfSatisfying(InvalidEmbeddingRequestException.class,
                        exception -> org.assertj.core.api.Assertions.assertThat(exception.errorCode())
                                .isEqualTo(ErrorCode.CHECKSUM_VALIDATION_FAILED));
    }
}
