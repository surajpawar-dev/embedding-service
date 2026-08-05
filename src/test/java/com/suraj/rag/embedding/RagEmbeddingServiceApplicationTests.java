package com.suraj.rag.embedding;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
        "job-store.mode=in-memory",
        "vector-store.mode=in-memory",
        "event-publisher.mode=logging",
        "embedding.provider=mock",
        "chunk-client.mode=local"
})
class RagEmbeddingServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
