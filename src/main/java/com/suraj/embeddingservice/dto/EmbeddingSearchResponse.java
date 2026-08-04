package com.suraj.embeddingservice.dto;

import java.util.List;

public record EmbeddingSearchResponse(List<EmbeddingSearchMatch> matches) {
}
