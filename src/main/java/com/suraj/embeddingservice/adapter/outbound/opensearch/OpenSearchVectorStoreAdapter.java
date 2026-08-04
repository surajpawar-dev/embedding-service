package com.suraj.embeddingservice.adapter.outbound.opensearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suraj.embeddingservice.config.OpenSearchProperties;
import com.suraj.embeddingservice.domain.EmbeddingVector;
import com.suraj.embeddingservice.exception.VectorStoreException;
import com.suraj.embeddingservice.port.outbound.VectorStorePort;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "vector-store", name = "mode", havingValue = "opensearch")
public class OpenSearchVectorStoreAdapter implements VectorStorePort {

    private static final TypeReference<Map<String, Object>> METADATA_TYPE = new TypeReference<>() {
    };

    private final RestClient restClient;
    private final OpenSearchProperties properties;
    private final ObjectMapper objectMapper;

    public OpenSearchVectorStoreAdapter(RestClient restClient, OpenSearchProperties properties, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Retryable(retryFor = VectorStoreException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public void upsertAll(List<EmbeddingVector> vectors) {
        if (vectors.isEmpty()) {
            return;
        }
        StringBuilder payload = new StringBuilder();
        for (EmbeddingVector vector : vectors) {
            appendBulkLine(payload, Map.of("index", Map.of(
                    "_index", properties.writeAlias(),
                    "_id", documentId(vector.documentId(), vector.chunkId(), vector.embeddingModel())
            )));
            appendBulkLine(payload, toDocument(vector, true));
        }
        Request request = new Request("POST", "/_bulk");
        request.addParameter("refresh", "false");
        request.setJsonEntity(payload.toString());
        try {
            Response response = restClient.performRequest(request);
            JsonNode body = objectMapper.readTree(EntityUtils.toString(response.getEntity()));
            if (body.path("errors").asBoolean(false)) {
                throw new VectorStoreException("OpenSearch bulk upsert reported item failures");
            }
        } catch (IOException exception) {
            throw new VectorStoreException("Failed to upsert vectors into OpenSearch", exception);
        }
    }

    @Override
    @Retryable(retryFor = VectorStoreException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public Optional<EmbeddingVector> findByChunkId(UUID chunkId) {
        Map<String, Object> body = Map.of(
                "size", 1,
                "query", Map.of("term", Map.of("chunkId", chunkId.toString()))
        );
        return executeSearch(body).stream().findFirst().map(SearchHit::vector);
    }

    @Override
    @Retryable(retryFor = VectorStoreException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public void deleteByChunkId(UUID chunkId) {
        Map<String, Object> body = Map.of(
                "query", Map.of("term", Map.of("chunkId", chunkId.toString()))
        );
        deleteByQuery(body, "Failed to delete vectors from OpenSearch");
    }

    @Override
    @Retryable(retryFor = VectorStoreException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public void deleteByDocumentIdAndModel(UUID documentId, String embeddingModel) {
        Map<String, Object> body = Map.of(
                "query", Map.of("bool", Map.of("filter", List.of(
                        Map.of("term", Map.of("documentId", documentId.toString())),
                        Map.of("term", Map.of("embeddingModel", embeddingModel))
                )))
        );
        deleteByQuery(body, "Failed to delete stale document vectors from OpenSearch");
    }

    @Override
    @Retryable(retryFor = VectorStoreException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public List<ScoredEmbeddingVector> search(float[] queryEmbedding, int topK, List<UUID> documentIds,
            String embeddingModel) {
        List<Object> filters = new ArrayList<>();
        filters.add(Map.of("term", Map.of("embeddingModel", embeddingModel)));
        if (documentIds != null && !documentIds.isEmpty()) {
            filters.add(Map.of("terms", Map.of("documentId", documentIds.stream().map(UUID::toString).toList())));
        }
        Map<String, Object> body = Map.of(
                "size", topK,
                "query", Map.of("bool", Map.of(
                        "filter", filters,
                        "must", List.of(Map.of("knn", Map.of("embedding", Map.of(
                                "vector", toList(queryEmbedding),
                                "k", topK
                        ))))
                )),
                "_source", Map.of("excludes", List.of("embedding"))
        );
        return executeSearch(body).stream()
                .map(hit -> new ScoredEmbeddingVector(hit.vector(), hit.score()))
                .toList();
    }

    private List<SearchHit> executeSearch(Map<String, Object> body) {
        Request request = new Request("POST", "/" + properties.readAlias() + "/_search");
        request.setJsonEntity(toJson(body));
        try {
            Response response = restClient.performRequest(request);
            JsonNode hits = objectMapper.readTree(EntityUtils.toString(response.getEntity()))
                    .path("hits")
                    .path("hits");
            List<SearchHit> results = new ArrayList<>();
            for (JsonNode hit : hits) {
                results.add(new SearchHit(toVector(hit.path("_source")), hit.path("_score").asDouble(0.0)));
            }
            return results;
        } catch (IOException exception) {
            throw new VectorStoreException("Failed to search vectors in OpenSearch", exception);
        }
    }

    private void deleteByQuery(Map<String, Object> body, String errorMessage) {
        Request request = new Request("POST", "/" + properties.writeAlias() + "/_delete_by_query");
        request.addParameter("refresh", "false");
        request.setJsonEntity(toJson(body));
        try {
            restClient.performRequest(request);
        } catch (IOException exception) {
            throw new VectorStoreException(errorMessage, exception);
        }
    }

    private Map<String, Object> toDocument(EmbeddingVector vector, boolean includeEmbedding) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("embeddingId", vector.embeddingId().toString());
        document.put("chunkId", vector.chunkId().toString());
        document.put("documentId", vector.documentId().toString());
        document.put("chunkOrder", vector.chunkOrder());
        document.put("content", vector.content());
        if (includeEmbedding) {
            document.put("embedding", toList(vector.embedding()));
        }
        document.put("embeddingModel", vector.embeddingModel());
        document.put("embeddingDimension", vector.embeddingDimension());
        document.put("pageNumber", vector.pageNumber());
        document.put("section", vector.section());
        document.put("title", vector.title());
        document.put("language", vector.language());
        document.put("source", vector.source());
        document.put("parentChunkId", vector.parentChunkId() == null ? null : vector.parentChunkId().toString());
        document.put("metadata", vector.metadata());
        document.put("documentChecksum", vector.documentChecksum());
        document.put("checksum", vector.checksum());
        document.put("createdAt", vector.createdAt().toString());
        document.put("updatedAt", Instant.now().toString());
        return document;
    }

    private EmbeddingVector toVector(JsonNode source) {
        return new EmbeddingVector(
                uuid(source, "embeddingId"),
                uuid(source, "chunkId"),
                uuid(source, "documentId"),
                intOrNull(source, "chunkOrder"),
                textOrNull(source, "content"),
                vectorOrEmpty(source.path("embedding")),
                textOrNull(source, "embeddingModel"),
                source.path("embeddingDimension").asInt(),
                intOrNull(source, "pageNumber"),
                textOrNull(source, "section"),
                textOrNull(source, "title"),
                textOrNull(source, "language"),
                textOrNull(source, "source"),
                uuidOrNull(source, "parentChunkId"),
                metadata(source.path("metadata")),
                textOrNull(source, "documentChecksum"),
                textOrNull(source, "checksum"),
                Instant.parse(textOrDefault(source, "createdAt", Instant.EPOCH.toString()))
        );
    }

    private String documentId(UUID documentId, UUID chunkId, String embeddingModel) {
        return documentId + ":" + chunkId + ":" + embeddingModel;
    }

    private Map<String, Object> metadata(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return Map.of();
        }
        return objectMapper.convertValue(node, METADATA_TYPE);
    }

    private void appendBulkLine(StringBuilder payload, Map<String, Object> line) {
        payload.append(toJson(line)).append('\n');
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new VectorStoreException("Failed to serialize OpenSearch request", exception);
        }
    }

    private List<Float> toList(float[] vector) {
        List<Float> values = new ArrayList<>(vector.length);
        for (float value : vector) {
            values.add(value);
        }
        return values;
    }

    private float[] vectorOrEmpty(JsonNode node) {
        if (!node.isArray()) {
            return new float[0];
        }
        float[] vector = new float[node.size()];
        for (int i = 0; i < node.size(); i++) {
            vector[i] = (float) node.get(i).asDouble();
        }
        return vector;
    }

    private UUID uuid(JsonNode source, String field) {
        return UUID.fromString(source.path(field).asText());
    }

    private UUID uuidOrNull(JsonNode source, String field) {
        String value = textOrNull(source, field);
        return value == null ? null : UUID.fromString(value);
    }

    private Integer intOrNull(JsonNode source, String field) {
        return source.path(field).isNumber() ? source.path(field).asInt() : null;
    }

    private String textOrNull(JsonNode source, String field) {
        JsonNode value = source.path(field);
        return value.isMissingNode() || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private String textOrDefault(JsonNode source, String field, String defaultValue) {
        String value = textOrNull(source, field);
        return value == null ? defaultValue : value;
    }

    private record SearchHit(EmbeddingVector vector, double score) {
    }
}
