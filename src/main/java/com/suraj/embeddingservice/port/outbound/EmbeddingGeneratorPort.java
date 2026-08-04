package com.suraj.embeddingservice.port.outbound;

import java.util.List;

public interface EmbeddingGeneratorPort {
    List<float[]> embed(List<String> inputs, String model);
}
