package com.suraj.rag.embedding.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends EmbeddingException {

    public NotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }
}
