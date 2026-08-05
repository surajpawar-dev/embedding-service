package com.suraj.rag.embedding.repository;

import com.suraj.rag.embedding.entity.RetryLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetryLogJpaRepository extends JpaRepository<RetryLogEntity, UUID> {}
