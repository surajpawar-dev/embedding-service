package com.suraj.rag.embedding.exception;

import org.springframework.http.HttpStatus;

public class EmbeddingException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public EmbeddingException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public EmbeddingException(
            ErrorCode errorCode, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
