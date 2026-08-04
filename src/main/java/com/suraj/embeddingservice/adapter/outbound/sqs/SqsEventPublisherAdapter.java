package com.suraj.embeddingservice.adapter.outbound.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suraj.embeddingservice.config.AwsSqsProperties;
import com.suraj.embeddingservice.event.EmbeddingCreatedEvent;
import com.suraj.embeddingservice.exception.EmbeddingException;
import com.suraj.embeddingservice.exception.ErrorCode;
import com.suraj.embeddingservice.port.outbound.EventPublisherPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Component
@ConditionalOnProperty(prefix = "event-publisher", name = "mode", havingValue = "sqs")
public class SqsEventPublisherAdapter implements EventPublisherPort {

    private final SqsClient sqsClient;
    private final AwsSqsProperties properties;
    private final ObjectMapper objectMapper;

    public SqsEventPublisherAdapter(SqsClient sqsClient, AwsSqsProperties properties, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Retryable(retryFor = SqsException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public void publish(EmbeddingCreatedEvent event) {
        String queueUrl = properties.sqs().embeddingCreatedQueueUrl();
        if (queueUrl == null || queueUrl.isBlank()) {
            throw new EmbeddingException(ErrorCode.EVENT_PUBLISH_FAILED, HttpStatus.INTERNAL_SERVER_ERROR,
                    "EMBEDDING_CREATED_QUEUE_URL must be configured when event-publisher.mode=sqs");
        }
        SendMessageRequest.Builder request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(toJson(event));
        if (queueUrl.endsWith(".fifo")) {
            request.messageGroupId(event.documentId().toString())
                    .messageDeduplicationId(event.embeddingJobId().toString());
        }
        sqsClient.sendMessage(request.build());
    }

    private String toJson(EmbeddingCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new EmbeddingException(ErrorCode.EVENT_PUBLISH_FAILED, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to serialize embedding-created event", exception);
        }
    }
}
