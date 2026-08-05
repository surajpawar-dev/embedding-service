package com.suraj.rag.embedding.adapter.outbound;

import com.suraj.rag.embedding.dto.ChunkResponse;
import com.suraj.rag.embedding.port.outbound.ChunkClientPort;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "chunk-client", name = "mode", havingValue = "local")
public class LocalChunkClientAdapter implements ChunkClientPort {

    @Override
    public List<ChunkResponse> fetchChunks(UUID documentId, List<UUID> chunkIds) {
        return IntStream.range(0, chunkIds.size())
                .mapToObj(
                        index -> {
                            UUID chunkId = chunkIds.get(index);
                            return new ChunkResponse(
                                    chunkId,
                                    documentId,
                                    index,
                                    "Local development chunk content for " + chunkId,
                                    "local-checksum-" + chunkId,
                                    null,
                                    null,
                                    null,
                                    "en",
                                    "local",
                                    null,
                                    Map.of(),
                                    Instant.now());
                        })
                .toList();
    }

    @Override
    public List<ChunkResponse> fetchAllChunks(UUID documentId) {
        return fetchChunks(
                documentId,
                List.of(
                        UUID.nameUUIDFromBytes(
                                documentId.toString().getBytes(StandardCharsets.UTF_8))));
    }
}
