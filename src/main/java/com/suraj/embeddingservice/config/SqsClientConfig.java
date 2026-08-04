package com.suraj.embeddingservice.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SqsClientConfig {

    @Bean
    SqsClient sqsClient(AwsSqsProperties properties) {
        return SqsClient.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}
