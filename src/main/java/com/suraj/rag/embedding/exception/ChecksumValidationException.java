package com.suraj.rag.embedding.exception;

import org.springframework.http.HttpStatus;

public class ChecksumValidationException extends EmbeddingException {
    public ChecksumValidationException(String message) {
        super(ErrorCode.CHECKSUM_VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
