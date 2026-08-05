package com.suraj.rag.embedding.domain;

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
