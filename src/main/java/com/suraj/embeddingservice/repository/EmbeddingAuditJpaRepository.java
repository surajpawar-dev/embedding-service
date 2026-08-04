package com.suraj.embeddingservice.repository;

import com.suraj.embeddingservice.entity.EmbeddingAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbeddingAuditJpaRepository extends JpaRepository<EmbeddingAuditEntity, UUID> {
}
