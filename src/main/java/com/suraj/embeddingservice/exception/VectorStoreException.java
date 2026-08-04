package com.suraj.embeddingservice.exception;

import org.springframework.http.HttpStatus;

public class VectorStoreException extends EmbeddingException {
    public VectorStoreException(String message) {
        super(ErrorCode.VECTOR_STORE_FAILED, HttpStatus.BAD_GATEWAY, message);
    }

    public VectorStoreException(String message, Throwable cause) {
        super(ErrorCode.VECTOR_STORE_FAILED, HttpStatus.BAD_GATEWAY, message, cause);
    }
}
