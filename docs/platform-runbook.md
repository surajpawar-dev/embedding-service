# Document RAG Platform Runbook

## Services

The local platform is made of three Spring services:

| Service | Project path | Responsibility |
|---|---|---|
| `rag-upload-service` | `C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-upload-service` | Upload API, S3 object storage, upload metadata, processing trigger |
| `rag-document-processing-service` | `C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-document-processing-service` | PDF reading, text cleanup, chunking, chunk persistence, document-ready event |
| `rag-embedding-service` | `C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-embedding-service` | SQS consumer, chunk fetch, Ollama embeddings, OpenSearch vectors, retrieval API |

## Start Everything

```powershell
cd C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-embedding-service
docker compose -f docker-compose.platform.yml up --build
```

## Local Infrastructure

| Container | Purpose |
|---|---|
| `localstack` | S3 bucket `documents`, SQS queues `document-ready` and `embedding-created` |
| `document-processing-db` | PostgreSQL database for documents, chunks, and processing history |
| `embedding-db` | PostgreSQL database for embedding jobs and embedded chunk state |
| `opensearch` | Upload metadata and vector index storage |
| `ollama` | Embedding model runtime |
| `ollama-pull` | One-shot model pull for `nomic-embed-text` |

## Ports

| Endpoint | URL |
|---|---|
| Upload API | `http://localhost:8080` |
| Processing API | `http://localhost:8081` |
| Embedding API | `http://localhost:8082` |
| OpenSearch | `http://localhost:9200` |
| LocalStack | `http://localhost:4566` |
| Ollama | `http://localhost:11434` |

## Event Flow

1. `rag-upload-service` receives a PDF upload and writes the file to S3.
2. `rag-upload-service` calls `POST /documents/process` on `rag-document-processing-service`.
3. `rag-document-processing-service` reads the PDF from S3, extracts text, cleans it, chunks it, and stores document/chunk rows.
4. `rag-document-processing-service` publishes a `document-ready` SQS message.
5. `rag-embedding-service` consumes the SQS message, fetches chunks page by page, embeds with Ollama, and writes vectors to OpenSearch.

## Production Replacement Points

For production, replace the compose-local infrastructure with managed services:

- LocalStack S3 -> AWS S3
- LocalStack SQS -> AWS SQS with DLQs and visibility timeout larger than max embedding job runtime
- Compose PostgreSQL -> managed PostgreSQL
- Compose OpenSearch -> managed OpenSearch/Elasticsearch with k-NN enabled
- Compose Ollama -> a secured Ollama host or approved embedding provider

Keep the embedding model and vector dimension stable. Changing either requires a new vector index and document re-embedding.
