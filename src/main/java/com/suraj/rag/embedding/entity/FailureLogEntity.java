package com.suraj.rag.embedding.entity;

import com.suraj.rag.embedding.domain.FailureStage;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "failure_logs")
public class FailureLogEntity {

    @Id private UUID id;
    private UUID jobId;
    private UUID documentId;
    private UUID chunkId;

    @Enumerated(EnumType.STRING)
    private FailureStage failureStage;

    private String errorCode;
    private String errorMessage;
    private String stackTrace;
    private String payload;
    private Boolean permanent;
    private Instant createdAt;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public void setFailureStage(FailureStage failureStage) {
        this.failureStage = failureStage;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setPermanent(Boolean permanent) {
        this.permanent = permanent;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
