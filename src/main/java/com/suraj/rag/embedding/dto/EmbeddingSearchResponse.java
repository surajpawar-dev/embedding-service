package com.suraj.rag.embedding.dto;

import java.util.List;

public record EmbeddingSearchResponse(List<EmbeddingSearchMatch> matches) {}
