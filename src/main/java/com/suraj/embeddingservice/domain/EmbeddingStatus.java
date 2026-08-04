package com.suraj.embeddingservice.domain;

public enum EmbeddingStatus {
    RECEIVED,
    FETCHING_CHUNKS,
    EMBEDDING,
    STORING,
    READY,
    PENDING,
    PROCESSING,
    COMPLETED,
    PARTIAL_FAILED,
    FAILED,
    SKIPPED,
    DELETED,
    CANCELLED
}
