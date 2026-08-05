package com.suraj.rag.embedding.adapter.outbound;

import com.suraj.rag.embedding.config.DocumentServiceProperties;
import com.suraj.rag.embedding.dto.ChunkResponse;
import com.suraj.rag.embedding.dto.DocumentResponse;
import com.suraj.rag.embedding.dto.PageResponse;
import com.suraj.rag.embedding.exception.ChunkFetchException;
import com.suraj.rag.embedding.port.outbound.ChunkClientPort;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(prefix = "chunk-client", name = "mode", havingValue = "document-service")
public class DocumentProcessingChunkClientAdapter implements ChunkClientPort {

    private static final ParameterizedTypeReference<PageResponse<ChunkResponse>> CHUNK_PAGE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestTemplate documentServiceRestTemplate;
    private final DocumentServiceProperties properties;

    public DocumentProcessingChunkClientAdapter(
            RestTemplate documentServiceRestTemplate, DocumentServiceProperties properties) {
        this.documentServiceRestTemplate = documentServiceRestTemplate;
        this.properties = properties;
    }

    @Override
    @Retryable(
            retryFor = ChunkFetchException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public List<ChunkResponse> fetchChunks(UUID documentId, List<UUID> chunkIds) {
        return fetchAllChunks(documentId).stream()
                .filter(chunk -> chunkIds.contains(chunk.id()))
                .toList();
    }

    @Override
    @Retryable(
            retryFor = ChunkFetchException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public List<ChunkResponse> fetchAllChunks(UUID documentId) {
        fetchDocumentMetadata(documentId);
        List<ChunkResponse> chunks = new ArrayList<>();
        int page = 0;
        boolean last = false;
        while (!last) {
            PageResponse<ChunkResponse> response = fetchChunkPage(documentId, page);
            chunks.addAll(response.content());
            last = response.last();
            page++;
        }
        return chunks.stream()
                .sorted((left, right) -> Integer.compare(left.chunkOrder(), right.chunkOrder()))
                .toList();
    }

    private DocumentResponse fetchDocumentMetadata(UUID documentId) {
        try {
            return documentServiceRestTemplate.getForObject(
                    "/documents/{documentId}", DocumentResponse.class, documentId);
        } catch (RestClientException exception) {
            throw new ChunkFetchException(
                    "Failed to fetch document metadata from Document Processing Service",
                    exception);
        }
    }

    private PageResponse<ChunkResponse> fetchChunkPage(UUID documentId, int page) {
        try {
            ResponseEntity<PageResponse<ChunkResponse>> response =
                    documentServiceRestTemplate.exchange(
                            "/documents/{documentId}/chunks?page={page}&size={size}",
                            HttpMethod.GET,
                            null,
                            CHUNK_PAGE_TYPE,
                            documentId,
                            page,
                            properties.pageSize());
            PageResponse<ChunkResponse> body = response.getBody();
            if (body == null) {
                throw new ChunkFetchException(
                        "Document Processing Service returned an empty chunk page");
            }
            return body;
        } catch (RestClientException exception) {
            throw new ChunkFetchException(
                    "Failed to fetch document chunks from Document Processing Service", exception);
        }
    }
}
