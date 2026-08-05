package com.suraj.rag.embedding.exception;

import org.springframework.http.HttpStatus;

public class EmbeddingGenerationException extends EmbeddingException {
    public EmbeddingGenerationException(String message) {
        super(ErrorCode.EMBEDDING_GENERATION_FAILED, HttpStatus.BAD_GATEWAY, message);
    }

    public EmbeddingGenerationException(String message, Throwable cause) {
        super(ErrorCode.EMBEDDING_GENERATION_FAILED, HttpStatus.BAD_GATEWAY, message, cause);
    }
}
