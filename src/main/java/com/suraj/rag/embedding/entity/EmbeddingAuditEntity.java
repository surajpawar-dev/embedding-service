package com.suraj.rag.embedding.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "embedding_audit")
public class EmbeddingAuditEntity {

    @Id
    private UUID id;
    private UUID jobId;
    private UUID chunkId;
    private UUID documentId;
    private String action;
    private String statusBefore;
    private String statusAfter;
    private String details;
    private String correlationId;
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

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setStatusBefore(String statusBefore) {
        this.statusBefore = statusBefore;
    }

    public void setStatusAfter(String statusAfter) {
        this.statusAfter = statusAfter;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
