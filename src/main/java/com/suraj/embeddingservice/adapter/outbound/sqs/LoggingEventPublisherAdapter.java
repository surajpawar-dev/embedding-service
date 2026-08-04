package com.suraj.embeddingservice.adapter.outbound.sqs;

import com.suraj.embeddingservice.event.EmbeddingCreatedEvent;
import com.suraj.embeddingservice.port.outbound.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "event-publisher", name = "mode", havingValue = "logging")
public class LoggingEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisherAdapter.class);

    @Override
    public void publish(EmbeddingCreatedEvent event) {
        log.info("Embedding event publishing is not wired yet: documentId={}, jobId={}, status={}",
                event.documentId(), event.embeddingJobId(), event.status());
    }
}
