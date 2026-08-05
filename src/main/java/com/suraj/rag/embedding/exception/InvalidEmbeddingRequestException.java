package com.suraj.rag.embedding.exception;

import org.springframework.http.HttpStatus;

public class InvalidEmbeddingRequestException extends EmbeddingException {

    public InvalidEmbeddingRequestException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.BAD_REQUEST, message);
    }
}
