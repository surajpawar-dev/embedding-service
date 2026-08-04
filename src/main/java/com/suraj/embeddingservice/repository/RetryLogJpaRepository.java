package com.suraj.embeddingservice.repository;

import com.suraj.embeddingservice.entity.RetryLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetryLogJpaRepository extends JpaRepository<RetryLogEntity, UUID> {
}
