package com.suraj.rag.embedding.repository;

import com.suraj.rag.embedding.entity.EmbeddingAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbeddingAuditJpaRepository extends JpaRepository<EmbeddingAuditEntity, UUID> {
}
