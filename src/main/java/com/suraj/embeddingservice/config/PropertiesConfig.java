package com.suraj.embeddingservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        EmbeddingProperties.class,
        DocumentServiceProperties.class,
        OllamaProperties.class,
        OpenSearchProperties.class,
        AwsSqsProperties.class,
        SecurityProperties.class
})
public class PropertiesConfig {
}
