package com.suraj.rag.embedding.domain;

import org.springframework.stereotype.Component;

@Component
public class EmbeddingStatusPolicy {

    public EmbeddingStatus aggregate(long total, long completed, long failed) {
        if (total == 0) {
            return EmbeddingStatus.PENDING;
        }
        if (failed > 0 && completed > 0) {
            return EmbeddingStatus.PARTIAL_FAILED;
        }
        if (failed > 0) {
            return EmbeddingStatus.FAILED;
        }
        if (completed == total) {
            return EmbeddingStatus.READY;
        }
        return EmbeddingStatus.PROCESSING;
    }
}
