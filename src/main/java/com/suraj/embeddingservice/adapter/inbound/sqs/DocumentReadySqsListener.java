package com.suraj.embeddingservice.adapter.inbound.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suraj.embeddingservice.config.AwsSqsProperties;
import com.suraj.embeddingservice.event.DocumentReadyEvent;
import com.suraj.embeddingservice.port.inbound.StartDocumentEmbeddingUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
@ConditionalOnProperty(prefix = "aws.sqs", name = "listener-enabled", havingValue = "true")
public class DocumentReadySqsListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentReadySqsListener.class);

    private final SqsClient sqsClient;
    private final AwsSqsProperties properties;
    private final ObjectMapper objectMapper;
    private final StartDocumentEmbeddingUseCase startDocumentEmbeddingUseCase;

    public DocumentReadySqsListener(SqsClient sqsClient, AwsSqsProperties properties, ObjectMapper objectMapper,
            StartDocumentEmbeddingUseCase startDocumentEmbeddingUseCase) {
        this.sqsClient = sqsClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.startDocumentEmbeddingUseCase = startDocumentEmbeddingUseCase;
    }

    @Scheduled(fixedDelayString = "${aws.sqs.poll-delay-ms:1000}")
    public void poll() {
        String queueUrl = documentReadyQueueUrl();
        if (queueUrl.isBlank()) {
            log.warn("SQS listener enabled but no document-ready queue URL is configured");
            return;
        }
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(properties.sqs().maxMessages())
                .waitTimeSeconds(properties.sqs().waitTimeSeconds())
                .visibilityTimeout(properties.sqs().visibilityTimeoutSeconds())
                .build();
        for (Message message : sqsClient.receiveMessage(request).messages()) {
            handleMessage(queueUrl, message);
        }
    }

    private void handleMessage(String queueUrl, Message message) {
        try {
            DocumentReadyEvent event = objectMapper.readValue(message.body(), DocumentReadyEvent.class);
            startDocumentEmbeddingUseCase.handleDocumentReady(event);
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (JsonProcessingException exception) {
            log.error("Invalid document-ready SQS message. Leaving message for DLQ redrive: messageId={}",
                    message.messageId(), exception);
        } catch (RuntimeException exception) {
            log.error("Document-ready SQS processing failed. Message will become visible again: messageId={}",
                    message.messageId(), exception);
        }
    }

    private String documentReadyQueueUrl() {
        String queueUrl = properties.sqs().documentReadyQueueUrl();
        if (queueUrl == null || queueUrl.isBlank()) {
            queueUrl = properties.sqs().chunksCreatedQueueUrl();
        }
        return queueUrl == null ? "" : queueUrl;
    }
}
