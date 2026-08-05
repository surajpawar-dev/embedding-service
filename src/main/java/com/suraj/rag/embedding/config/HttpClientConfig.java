package com.suraj.rag.embedding.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    @Bean
    RestTemplate documentServiceRestTemplate(RestTemplateBuilder builder, DocumentServiceProperties properties) {
        return builder
                .rootUri(properties.baseUrl())
                .setConnectTimeout(properties.requestTimeout())
                .setReadTimeout(properties.requestTimeout())
                .build();
    }

    @Bean
    RestTemplate ollamaRestTemplate(RestTemplateBuilder builder, OllamaProperties properties) {
        return builder
                .rootUri(properties.baseUrl())
                .setConnectTimeout(properties.requestTimeout())
                .setReadTimeout(properties.requestTimeout())
                .build();
    }
}
