package com.suraj.rag.embedding.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "retry_logs")
public class RetryLogEntity {

    @Id private UUID id;
    private UUID jobId;
    private UUID chunkId;
    private Integer attemptNumber;
    private String retryReason;
    private String errorMessage;
    private Instant nextRetryAt;
    private Instant createdAt;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public void setRetryReason(String retryReason) {
        this.retryReason = retryReason;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setNextRetryAt(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
