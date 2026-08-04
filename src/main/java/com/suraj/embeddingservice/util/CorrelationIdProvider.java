package com.suraj.embeddingservice.util;

import com.suraj.embeddingservice.common.MdcKeys;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class CorrelationIdProvider {

    public String currentOrNew() {
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        return correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId;
    }
}
