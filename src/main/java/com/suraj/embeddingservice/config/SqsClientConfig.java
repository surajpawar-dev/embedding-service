package com.suraj.embeddingservice.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsClientConfig {

    @Bean
    SqsClient sqsClient(AwsSqsProperties properties) {
        var builder = SqsClient.builder().region(Region.of(properties.region()));

        if (properties.sqs().endpoint() != null && !properties.sqs().endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.sqs().endpoint()));
        }

        return builder.build();
    }
}
