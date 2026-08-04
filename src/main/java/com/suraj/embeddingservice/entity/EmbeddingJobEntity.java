package com.suraj.embeddingservice.entity;

import com.suraj.embeddingservice.domain.EmbeddingStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "embedding_jobs")
public class EmbeddingJobEntity {

    @Id
    private UUID id;
    private UUID documentId;
    private Integer totalChunks;
    private Integer completedChunks;
    private Integer failedChunks;
    private String embeddingModel;
    private Integer embeddingDimension;
    @Enumerated(EnumType.STRING)
    private EmbeddingStatus status;
    private String correlationId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public Integer getCompletedChunks() {
        return completedChunks;
    }

    public void setCompletedChunks(Integer completedChunks) {
        this.completedChunks = completedChunks;
    }

    public Integer getFailedChunks() {
        return failedChunks;
    }

    public void setFailedChunks(Integer failedChunks) {
        this.failedChunks = failedChunks;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Integer getEmbeddingDimension() {
        return embeddingDimension;
    }

    public void setEmbeddingDimension(Integer embeddingDimension) {
        this.embeddingDimension = embeddingDimension;
    }

    public EmbeddingStatus getStatus() {
        return status;
    }

    public void setStatus(EmbeddingStatus status) {
        this.status = status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
