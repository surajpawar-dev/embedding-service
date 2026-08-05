package com.suraj.rag.embedding.exception;

import org.springframework.http.HttpStatus;

public class ChunkFetchException extends EmbeddingException {
    public ChunkFetchException(String message) {
        super(ErrorCode.CHUNK_FETCH_FAILED, HttpStatus.BAD_GATEWAY, message);
    }

    public ChunkFetchException(String message, Throwable cause) {
        super(ErrorCode.CHUNK_FETCH_FAILED, HttpStatus.BAD_GATEWAY, message, cause);
    }
}
