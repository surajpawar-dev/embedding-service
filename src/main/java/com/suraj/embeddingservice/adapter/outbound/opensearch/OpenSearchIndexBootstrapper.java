package com.suraj.embeddingservice.adapter.outbound.opensearch;

import com.suraj.embeddingservice.config.OpenSearchProperties;
import com.suraj.embeddingservice.exception.VectorStoreException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.opensearch.client.Request;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "vector-store", name = "mode", havingValue = "opensearch")
public class OpenSearchIndexBootstrapper implements ApplicationRunner {

    private final RestClient restClient;
    private final OpenSearchProperties properties;

    public OpenSearchIndexBootstrapper(RestClient restClient, OpenSearchProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.bootstrap().enabled()) {
            return;
        }
        if (indexExists()) {
            return;
        }
        Request request = new Request("PUT", "/" + properties.bootstrap().indexName());
        request.setJsonEntity(indexDefinition());
        try {
            restClient.performRequest(request);
        } catch (IOException exception) {
            throw new VectorStoreException("Failed to create OpenSearch vector index", exception);
        }
    }

    private boolean indexExists() {
        try {
            restClient.performRequest(new Request("HEAD", "/" + properties.bootstrap().indexName()));
            return true;
        } catch (ResponseException exception) {
            if (exception.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            }
            throw new VectorStoreException("Failed to check OpenSearch vector index", exception);
        } catch (IOException exception) {
            throw new VectorStoreException("Failed to check OpenSearch vector index", exception);
        }
    }

    private String indexDefinition() {
        try {
            ClassPathResource resource = new ClassPathResource("opensearch/document_embeddings_768.json");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new VectorStoreException("Failed to load OpenSearch index definition", exception);
        }
    }
}
