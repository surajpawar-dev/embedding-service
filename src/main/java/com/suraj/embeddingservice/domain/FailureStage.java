package com.suraj.embeddingservice.domain;

public enum FailureStage {
    FETCH_CHUNK,
    CHECKSUM,
    EMBEDDING,
    OPENSEARCH,
    POSTGRES,
    PUBLISH_EVENT,
    UNKNOWN
}
