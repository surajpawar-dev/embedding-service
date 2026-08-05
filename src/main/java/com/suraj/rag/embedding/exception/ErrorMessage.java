package com.suraj.rag.embedding.exception;

public final class ErrorMessage {

    public static final String UNEXPECTED_SERVICE_FAILURE = "Unexpected service failure";
    public static final String REQUEST_VALIDATION_FAILED = "Request validation failed";
    public static final String DUPLICATE_CHUNK_IDS = "Request contains duplicate chunk IDs";
    public static final String INCOMPLETE_CHUNK_SET =
            "Document Processing Service returned an incomplete chunk set";
    public static final String EMPTY_CHUNK_CONTENT = "Chunk content must not be blank";
    public static final String EMPTY_CHUNK_CHECKSUM = "Chunk checksum must not be blank";
    public static final String EMBEDDING_COUNT_MISMATCH =
            "Embedding provider returned a different number of vectors than requested";
    public static final String EMBEDDING_DIMENSION_MISMATCH =
            "Embedding provider returned a vector with an unexpected dimension";

    private ErrorMessage() {}
}
