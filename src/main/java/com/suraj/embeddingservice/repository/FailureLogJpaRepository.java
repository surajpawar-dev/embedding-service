package com.suraj.embeddingservice.repository;

import com.suraj.embeddingservice.entity.FailureLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailureLogJpaRepository extends JpaRepository<FailureLogEntity, UUID> {
}
