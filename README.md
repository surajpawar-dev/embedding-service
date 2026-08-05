# Embedding Service

Spring Boot service that consumes ready document chunks, generates embeddings with Ollama, stores vectors in OpenSearch, persists embedding job state in PostgreSQL, and exposes retrieval APIs for downstream RAG services.

## Production Responsibilities

This service owns:

- Consuming document-ready events from the Document Processing Service.
- Fetching document metadata and paginated chunks from the Document Processing Service.
- Generating embeddings through Ollama.
- Persisting embedding job and chunk status in PostgreSQL.
- Storing and searching vectors in OpenSearch.
- Exposing manual embedding, status, and semantic search APIs.

It does not parse PDFs, clean text, chunk documents, or modify records in the Document Processing Service.

## Runtime Flow

1. Document Processing Service finishes processing a PDF and marks the document `READY`.
2. It emits a document-ready event.
3. Embedding Service creates an embedding job in PostgreSQL.
4. It fetches chunks page by page from `GET /documents/{documentId}/chunks?page=0&size=200`.
5. It calls Ollama `/api/embeddings` using the configured model.
6. It bulk upserts vector documents into OpenSearch using deterministic IDs: `documentId:chunkId:embeddingModel`.
7. It marks the job `READY`.
8. Search requests embed the query with the same model and perform OpenSearch k-NN search.

Example document-ready event:

```json
{
  "documentId": "bde09e5d-608d-43ad-9048-6dce424fcad0",
  "checksum": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "chunkCount": 42,
  "readyAt": "2026-08-03T14:30:00Z"
}
```

## Production Defaults

`application.yml` is configured for production-oriented defaults:

| Setting | Default |
|---|---|
| `embedding.provider` | `ollama` |
| `chunk-client.mode` | `document-service` |
| `job-store.mode` | `jpa` |
| `vector-store.mode` | `opensearch` |
| `app.ollama.embedding-model` | `nomic-embed-text` |
| `embedding.dimension` | `768` |

Use `mock`, `local`, or `in-memory` only for tests or isolated local development.

## Required Services

- Java 21+
- PostgreSQL
- OpenSearch with k-NN enabled
- Ollama with the selected embedding model pulled
- Document Processing Service

## Higher Environment Infrastructure

For local platform runs, the parent `document-rag-platform/docker-compose.yml` provides PostgreSQL, LocalStack, OpenSearch, Ollama, and the Document Processing Service. The service runs with:

```text
SPRING_PROFILES_ACTIVE=local
```

In local profile, SQS polling is disabled by default for standalone test runs. The parent Docker Compose file explicitly enables it with:

```text
SQS_LISTENER_ENABLED=true
```

For dev, staging, or production, run with:

```text
SPRING_PROFILES_ACTIVE=prod
```

Do not use LocalStack endpoint values in higher environments. Leave `AWS_SQS_ENDPOINT` empty or unset so the AWS SDK uses real AWS SQS endpoints for `AWS_REGION`.

### PostgreSQL

Create a dedicated database for embedding jobs, chunk embedding status, retry logs, and audit records.

```text
POSTGRES_HOST=embedding-db.prod.internal
POSTGRES_PORT=5432
POSTGRES_DB=embedding
POSTGRES_USER=<from-secret>
POSTGRES_PASSWORD=<from-secret>
```

Production notes:

- Use a managed PostgreSQL service where possible.
- Enable backups and point-in-time recovery.
- Use TLS for database traffic.
- Keep one database/schema per environment.
- Flyway migrations are included under `src/main/resources/db/migration`.

### OpenSearch

OpenSearch stores embedding vectors and serves semantic search.

```text
VECTOR_STORE_MODE=opensearch
OPENSEARCH_ENDPOINT=https://opensearch-prod.example.com
OPENSEARCH_USERNAME=<from-secret>
OPENSEARCH_PASSWORD=<from-secret>
OPENSEARCH_READ_ALIAS=document_embeddings_read
OPENSEARCH_WRITE_ALIAS=document_embeddings_write
```

The OpenSearch index dimension must match the embedding model:

```text
OLLAMA_EMBEDDING_MODEL=nomic-embed-text
EMBEDDING_DIMENSION=768
```

If the model or dimension changes, create a new vector index and re-embed documents. Do not mix vectors from different dimensions in the same index.

### Ollama Or Embedding Model Runtime

The service calls Ollama-compatible embedding APIs.

```text
OLLAMA_BASE_URL=https://ollama-prod.internal.example.com
OLLAMA_EMBEDDING_MODEL=nomic-embed-text
OLLAMA_REQUEST_TIMEOUT=PT30S
```

Production notes:

- Keep the model version stable for an index.
- Ensure enough CPU/GPU capacity for expected queue volume.
- Use internal networking and TLS where available.
- Scale embedding workers based on queue depth, OpenSearch bulk latency, and model latency.

### Amazon SQS

The service consumes document-ready events and can publish embedding-created events.

```text
AWS_REGION=us-east-1
AWS_SQS_ENDPOINT=
SQS_LISTENER_ENABLED=true
DOCUMENT_READY_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/<account-id>/document-ready-prod
EMBEDDING_CREATED_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/<account-id>/embedding-created-prod
EVENT_PUBLISHER_MODE=sqs
```

Required AWS permissions:

```text
sqs:ReceiveMessage
sqs:DeleteMessage
sqs:ChangeMessageVisibility
sqs:SendMessage
sqs:GetQueueAttributes
```

Queue recommendations:

- Configure a dead-letter queue for `document-ready`.
- Set visibility timeout longer than the maximum expected embedding job time.
- Keep queue names environment-specific.
- Monitor queue depth, DLQ depth, receive errors, and processing latency.

### Document Processing Service

The service fetches document metadata and chunks from `rag-document-processing-service`.

```text
DOCUMENT_SERVICE_BASE_URL=https://document-processing.internal.example.com
DOCUMENT_SERVICE_CHUNK_PAGE_SIZE=200
DOCUMENT_SERVICE_REQUEST_TIMEOUT=PT30S
CHUNK_CLIENT_MODE=document-service
```

Use an internal service URL or internal load balancer. This service does not need public internet access to the Document Processing Service.

### Security

For shared or public higher environments:

```text
SECURITY_JWT_ENABLED=true
```

Then provide the resource server/JWT issuer configuration expected by the deployment. Keep `SECURITY_JWT_ENABLED=false` only for local development or isolated private environments.

### Production Example

```text
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

POSTGRES_HOST=embedding-db.prod.internal
POSTGRES_PORT=5432
POSTGRES_DB=embedding
POSTGRES_USER=<from-secret>
POSTGRES_PASSWORD=<from-secret>

DOCUMENT_SERVICE_BASE_URL=https://document-processing.internal.example.com

OLLAMA_BASE_URL=https://ollama-prod.internal.example.com
OLLAMA_EMBEDDING_MODEL=nomic-embed-text
EMBEDDING_PROVIDER=ollama
EMBEDDING_DIMENSION=768
EMBEDDING_BATCH_SIZE=128

VECTOR_STORE_MODE=opensearch
OPENSEARCH_ENDPOINT=https://opensearch-prod.example.com
OPENSEARCH_USERNAME=<from-secret>
OPENSEARCH_PASSWORD=<from-secret>

AWS_REGION=us-east-1
AWS_SQS_ENDPOINT=
SQS_LISTENER_ENABLED=true
DOCUMENT_READY_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/<account-id>/document-ready-prod
EMBEDDING_CREATED_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/<account-id>/embedding-created-prod

JOB_STORE_MODE=jpa
CHUNK_CLIENT_MODE=document-service
EVENT_PUBLISHER_MODE=sqs
SECURITY_JWT_ENABLED=true
```

AWS credentials should come from the hosting platform IAM role, not from hardcoded access keys.

## Environment Variables

```bash
SERVER_PORT=8080

POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=embedding
POSTGRES_USER=embedding
POSTGRES_PASSWORD=embedding

DOCUMENT_SERVICE_BASE_URL=http://localhost:8081
DOCUMENT_SERVICE_CHUNK_PAGE_SIZE=200
DOCUMENT_SERVICE_REQUEST_TIMEOUT=PT30S

OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_EMBEDDING_MODEL=nomic-embed-text
OLLAMA_REQUEST_TIMEOUT=PT30S

EMBEDDING_PROVIDER=ollama
EMBEDDING_DIMENSION=768
EMBEDDING_BATCH_SIZE=128

VECTOR_STORE_MODE=opensearch
OPENSEARCH_ENDPOINT=http://localhost:9200
OPENSEARCH_USERNAME=admin
OPENSEARCH_PASSWORD=admin
OPENSEARCH_READ_ALIAS=document_embeddings_read
OPENSEARCH_WRITE_ALIAS=document_embeddings_write

JOB_STORE_MODE=jpa
CHUNK_CLIENT_MODE=document-service
```

Important: `OLLAMA_EMBEDDING_MODEL` and `EMBEDDING_DIMENSION` must match the model. If the model changes, create a new OpenSearch index with the new dimension and re-embed documents.

## Setup

### Full Three-Service Platform

Use this when you want the local stack to behave like production wiring:

```powershell
cd C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-embedding-service
docker compose -f docker-compose.platform.yml up --build
```

This starts:

| Component | Host port | Purpose |
|---|---:|---|
| `rag-upload-service` | `8080` | Accepts uploads, stores files in S3, triggers processing |
| `rag-document-processing-service` | `8081` | Reads PDFs from S3, cleans/chunks text, stores chunks |
| `rag-embedding-service` | `8082` | Consumes SQS events, embeds chunks, stores/searches vectors |
| LocalStack | `4566` | Local S3 bucket `documents` and SQS queues |
| OpenSearch | `9200` | Upload metadata and embedding vectors |
| Ollama | `11434` | Local embedding model runtime |
| Processing PostgreSQL | `5433` | Document/chunk relational state |
| Embedding PostgreSQL | `5434` | Embedding job/chunk relational state |

Expected local flow:

1. Upload a PDF through `rag-upload-service`.
2. `rag-upload-service` stores the PDF in LocalStack S3 and calls `rag-document-processing-service`.
3. `rag-document-processing-service` extracts, cleans, chunks, persists, then publishes `document-ready` to SQS.
4. `rag-embedding-service` consumes the event, fetches chunks over HTTP, generates Ollama embeddings, and upserts vectors into OpenSearch.
5. Query vectors through `POST http://localhost:8082/api/v1/embeddings/search`.

The compose file uses source builds for all three services:

- `C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-embedding-service`
- `C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-document-processing-service`
- `C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-upload-service`

Before running, make sure Docker Desktop is running and the ports above are free. The first run can take time because Docker pulls PostgreSQL, OpenSearch, LocalStack, Ollama, and the `nomic-embed-text` model.

### Embedding Service Only

1. Build the service:

```bash
.\mvnw.cmd package
```

2. Pull the Ollama model:

```bash
ollama pull nomic-embed-text
```

3. Start dependencies and service locally:

```bash
docker compose up --build
```

Flyway applies the PostgreSQL schema automatically from `src/main/resources/db/migration`. The service creates the OpenSearch index automatically from `src/main/resources/opensearch/document_embeddings_768.json` when `OPENSEARCH_BOOTSTRAP_ENABLED=true`.

Manual schema/index files are also available for controlled production rollout:

- `docs/postgres-schema.sql`
- `docs/opensearch-index.json`

4. Run tests locally:

```bash
.\mvnw.cmd test
```

5. Build the jar:

```bash
.\mvnw.cmd package
```

6. Start the service:

```bash
java -jar target/rag-embedding-service-0.0.1-SNAPSHOT.jar
```

## Production Deployment Checklist

Before deploying, make sure these infrastructure values exist:

- PostgreSQL database and credentials.
- OpenSearch endpoint with k-NN enabled.
- Ollama endpoint with `nomic-embed-text` pulled, or another model with matching `EMBEDDING_DIMENSION`.
- Document Processing Service reachable from this service.
- SQS document-ready queue URL when `SQS_LISTENER_ENABLED=true`.
- SQS embedding-created queue URL when `EVENT_PUBLISHER_MODE=sqs`.
- JWT issuer/audience/resource-server config if `SECURITY_JWT_ENABLED=true`.

## APIs

### Start or Retry Document Embedding

```http
POST /api/v1/embeddings/documents/{documentId}
```

### Consume Document-Ready Event

```http
POST /api/v1/embeddings/events/document-ready
Content-Type: application/json

{
  "documentId": "bde09e5d-608d-43ad-9048-6dce424fcad0",
  "checksum": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "chunkCount": 42,
  "readyAt": "2026-08-03T14:30:00Z"
}
```

### Get Document Embedding Status

```http
GET /api/v1/embeddings/documents/{documentId}/status
```

### Search Vectors

```http
POST /api/v1/embeddings/search
Content-Type: application/json

{
  "query": "What are the cancellation terms?",
  "topK": 5,
  "documentIds": ["bde09e5d-608d-43ad-9048-6dce424fcad0"]
}
```

## Idempotency

- OpenSearch document ID is `documentId:chunkId:embeddingModel`.
- Reprocessing the same document-ready event overwrites the same vector records.
- PostgreSQL status rows are upserted by `chunkId + embeddingModel + checksum`.
- For document-level re-embedding, existing vectors for the same `documentId + embeddingModel` are deleted before current chunks are indexed. Keep the Document Processing Service event idempotent and retryable.

## Health and Monitoring

- Liveness/readiness: `/actuator/health`
- Metrics: `/actuator/prometheus`
- OpenSearch health indicator performs a `HEAD /` check when `VECTOR_STORE_MODE=opensearch`.

Track at minimum:

- embedding jobs accepted/completed/failed
- embeddings generated
- Ollama latency and failures
- OpenSearch bulk write latency and failures
- vector search latency
- queue retry count and DLQ depth

## Production Notes

- Use TLS for PostgreSQL, OpenSearch, and service-to-service calls.
- Do not log raw chunk content or embedding arrays.
- Keep the embedding model stable after vectors are stored.
- Size OpenSearch shards for vector workload and memory, not only document count.
- Use SQS visibility timeout longer than the maximum expected embedding job time.
- Enable JWT validation with `SECURITY_JWT_ENABLED=true` for public or shared environments.
