package com.suraj.rag.embedding.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "vector-store", name = "mode", havingValue = "opensearch")
public class OpenSearchClientConfig {

    @Bean(destroyMethod = "close")
    RestClient openSearchRestClient(OpenSearchProperties properties) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(properties.username(), properties.password()));
        return RestClient.builder(HttpHost.create(properties.endpoint()))
                .setHttpClientConfigCallback(
                        builder -> builder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
    }
}
