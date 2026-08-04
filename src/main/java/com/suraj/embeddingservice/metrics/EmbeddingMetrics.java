package com.suraj.embeddingservice.metrics;

import com.suraj.embeddingservice.common.MetricsNames;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingMetrics {

    private final Counter chunksProcessed;
    private final Counter chunksFailed;
    private final Counter jobsCompleted;
    private final Counter jobsFailed;
    private final Timer ollamaLatency;
    private final Timer vectorWriteLatency;
    private final Timer vectorSearchLatency;
    private final Timer chunkFetchLatency;

    public EmbeddingMetrics(MeterRegistry meterRegistry) {
        this.chunksProcessed = Counter.builder(MetricsNames.EMBEDDING_CHUNKS_PROCESSED_TOTAL).register(meterRegistry);
        this.chunksFailed = Counter.builder(MetricsNames.EMBEDDING_CHUNKS_FAILED_TOTAL).register(meterRegistry);
        this.jobsCompleted = Counter.builder("embedding_jobs_completed_total").register(meterRegistry);
        this.jobsFailed = Counter.builder("embedding_jobs_failed_total").register(meterRegistry);
        this.ollamaLatency = Timer.builder("ollama_embedding_latency").register(meterRegistry);
        this.vectorWriteLatency = Timer.builder("vector_store_write_latency").register(meterRegistry);
        this.vectorSearchLatency = Timer.builder("vector_search_latency").register(meterRegistry);
        this.chunkFetchLatency = Timer.builder("document_chunk_fetch_latency").register(meterRegistry);
    }

    public void incrementProcessed() {
        chunksProcessed.increment();
    }

    public void incrementFailed() {
        chunksFailed.increment();
    }

    public void incrementProcessed(double amount) {
        chunksProcessed.increment(amount);
    }

    public void incrementFailed(double amount) {
        chunksFailed.increment(amount);
    }

    public void incrementJobsCompleted() {
        jobsCompleted.increment();
    }

    public void incrementJobsFailed() {
        jobsFailed.increment();
    }

    public <T> T recordOllama(Supplier<T> supplier) {
        return ollamaLatency.record(supplier);
    }

    public <T> T recordVectorSearch(Supplier<T> supplier) {
        return vectorSearchLatency.record(supplier);
    }

    public <T> T recordChunkFetch(Supplier<T> supplier) {
        return chunkFetchLatency.record(supplier);
    }

    public void recordVectorWrite(Runnable runnable) {
        vectorWriteLatency.record(runnable);
    }
}
