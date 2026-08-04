package com.suraj.embeddingservice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmbeddingStatusPolicyTest {

    private final EmbeddingStatusPolicy policy = new EmbeddingStatusPolicy();

    @Test
    void returnsPendingWhenNoChunksExist() {
        assertThat(policy.aggregate(0, 0, 0)).isEqualTo(EmbeddingStatus.PENDING);
    }

    @Test
    void returnsPartialFailedWhenSomeChunksCompleteAndSomeFail() {
        assertThat(policy.aggregate(10, 8, 2)).isEqualTo(EmbeddingStatus.PARTIAL_FAILED);
    }

    @Test
    void returnsProcessingWhenWorkIsIncompleteWithoutFailures() {
        assertThat(policy.aggregate(10, 4, 0)).isEqualTo(EmbeddingStatus.PROCESSING);
    }
}
