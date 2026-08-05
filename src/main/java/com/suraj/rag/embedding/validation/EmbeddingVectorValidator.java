package com.suraj.rag.embedding.validation;

import com.suraj.rag.embedding.exception.ErrorCode;
import com.suraj.rag.embedding.exception.ErrorMessage;
import com.suraj.rag.embedding.exception.InvalidEmbeddingRequestException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingVectorValidator {

    public void validateEmbeddingResult(
            int inputCount, List<float[]> embeddings, int expectedDimension) {
        if (embeddings.size() != inputCount) {
            throw new InvalidEmbeddingRequestException(
                    ErrorCode.EMBEDDING_GENERATION_FAILED, ErrorMessage.EMBEDDING_COUNT_MISMATCH);
        }
        embeddings.stream()
                .filter(vector -> vector == null || vector.length != expectedDimension)
                .findFirst()
                .ifPresent(
                        vector -> {
                            throw new InvalidEmbeddingRequestException(
                                    ErrorCode.EMBEDDING_DIMENSION_MISMATCH,
                                    ErrorMessage.EMBEDDING_DIMENSION_MISMATCH);
                        });
    }
}
