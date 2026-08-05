package com.suraj.rag.embedding.entity;

import com.suraj.rag.embedding.domain.EmbeddingStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "embedding_status")
public class EmbeddingStatusEntity {

    @Id
    private UUID id;
    private UUID jobId;
    private UUID documentId;
    private UUID chunkId;
    private UUID embeddingId;
    private String embeddingModel;
    private Integer embeddingDimension;
    private String checksum;
    @Enumerated(EnumType.STRING)
    private EmbeddingStatus status;
    private String opensearchIndex;
    private String opensearchDocumentId;
    private Integer attemptCount;
    private String errorCode;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getChunkId() {
        return chunkId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public UUID getEmbeddingId() {
        return embeddingId;
    }

    public void setEmbeddingId(UUID embeddingId) {
        this.embeddingId = embeddingId;
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

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public EmbeddingStatus getStatus() {
        return status;
    }

    public void setStatus(EmbeddingStatus status) {
        this.status = status;
    }

    public String getOpensearchIndex() {
        return opensearchIndex;
    }

    public void setOpensearchIndex(String opensearchIndex) {
        this.opensearchIndex = opensearchIndex;
    }

    public String getOpensearchDocumentId() {
        return opensearchDocumentId;
    }

    public void setOpensearchDocumentId(String opensearchDocumentId) {
        this.opensearchDocumentId = opensearchDocumentId;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
