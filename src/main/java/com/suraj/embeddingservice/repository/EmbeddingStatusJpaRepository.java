package com.suraj.embeddingservice.repository;

import com.suraj.embeddingservice.entity.EmbeddingStatusEntity;
import com.suraj.embeddingservice.domain.EmbeddingStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbeddingStatusJpaRepository extends JpaRepository<EmbeddingStatusEntity, UUID> {
    Optional<EmbeddingStatusEntity> findFirstByChunkId(UUID chunkId);

    Optional<EmbeddingStatusEntity> findFirstByChunkIdAndEmbeddingModelAndChecksum(UUID chunkId, String embeddingModel,
            String checksum);

    long countByDocumentId(UUID documentId);

    long countByDocumentIdAndStatus(UUID documentId, EmbeddingStatus status);
}
