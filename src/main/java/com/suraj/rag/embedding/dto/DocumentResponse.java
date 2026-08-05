package com.suraj.rag.embedding.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String checksum,
        String status,
        Integer chunkCount,
        Instant createdAt,
        Instant updatedAt) {}
