package com.suraj.rag.embedding.health;

import java.io.IOException;
import org.opensearch.client.Request;
import org.opensearch.client.RestClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchHealthIndicator implements HealthIndicator {

    private final ObjectProvider<RestClient> restClientProvider;

    public OpenSearchHealthIndicator(ObjectProvider<RestClient> restClientProvider) {
        this.restClientProvider = restClientProvider;
    }

    @Override
    public Health health() {
        RestClient restClient = restClientProvider.getIfAvailable();
        if (restClient == null) {
            return Health.up().withDetail("adapter", "in-memory").build();
        }
        try {
            restClient.performRequest(new Request("HEAD", "/"));
            return Health.up().withDetail("adapter", "opensearch").build();
        } catch (IOException exception) {
            return Health.down(exception).withDetail("adapter", "opensearch").build();
        }
    }
}
