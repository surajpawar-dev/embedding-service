package com.suraj.rag.embedding.repository;

import com.suraj.rag.embedding.entity.EmbeddingJobEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbeddingJobJpaRepository extends JpaRepository<EmbeddingJobEntity, UUID> {
    Optional<EmbeddingJobEntity> findFirstByDocumentIdOrderByCreatedAtDesc(UUID documentId);
}
