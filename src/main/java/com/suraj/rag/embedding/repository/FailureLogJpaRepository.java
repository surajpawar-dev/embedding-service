package com.suraj.rag.embedding.repository;

import com.suraj.rag.embedding.entity.FailureLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailureLogJpaRepository extends JpaRepository<FailureLogEntity, UUID> {
}
