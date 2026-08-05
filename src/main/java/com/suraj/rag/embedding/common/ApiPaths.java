package com.suraj.rag.embedding.common;

public final class ApiPaths {

    public static final String API_V1 = "/api/v1";
    public static final String EMBEDDINGS = API_V1 + "/embeddings";
    public static final String EMBEDDINGS_BATCH = "/batch";
    public static final String EMBEDDINGS_DOCUMENTS = "/documents/{documentId}";
    public static final String EMBEDDINGS_DOCUMENT_STATUS = "/documents/{documentId}/status";
    public static final String EMBEDDINGS_SEARCH = "/search";
    public static final String EMBEDDINGS_DOCUMENT_READY_EVENTS = "/events/document-ready";
    public static final String STATUS = API_V1 + "/status";
    public static final String HEALTH = API_V1 + "/health";
    public static final String ACTUATOR_HEALTH = "/actuator/health/**";
    public static final String OPENAPI = "/v3/api-docs/**";
    public static final String SWAGGER_UI = "/swagger-ui/**";

    private ApiPaths() {}
}
