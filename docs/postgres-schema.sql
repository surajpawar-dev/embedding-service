CREATE TABLE IF NOT EXISTS embedding_jobs (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    total_chunks INTEGER NOT NULL CHECK (total_chunks >= 0),
    completed_chunks INTEGER NOT NULL DEFAULT 0 CHECK (completed_chunks >= 0),
    failed_chunks INTEGER NOT NULL DEFAULT 0 CHECK (failed_chunks >= 0),
    embedding_model VARCHAR(128) NOT NULL,
    embedding_dimension INTEGER NOT NULL CHECK (embedding_dimension > 0),
    status VARCHAR(32) NOT NULL,
    correlation_id VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS embedding_status (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    document_id UUID NOT NULL,
    chunk_id UUID NOT NULL,
    embedding_id UUID,
    embedding_model VARCHAR(128) NOT NULL,
    embedding_dimension INTEGER NOT NULL CHECK (embedding_dimension > 0),
    checksum VARCHAR(256) NOT NULL,
    status VARCHAR(32) NOT NULL,
    opensearch_index VARCHAR(128),
    opensearch_document_id VARCHAR(512),
    attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    error_code VARCHAR(128),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_embedding_status_job FOREIGN KEY (job_id) REFERENCES embedding_jobs(id) ON DELETE CASCADE,
    CONSTRAINT uq_embedding_status_chunk_model_checksum UNIQUE (chunk_id, embedding_model, checksum)
);

CREATE TABLE IF NOT EXISTS embedding_audit (
    id UUID PRIMARY KEY,
    job_id UUID REFERENCES embedding_jobs(id) ON DELETE SET NULL,
    chunk_id UUID,
    document_id UUID,
    action VARCHAR(64) NOT NULL,
    status_before VARCHAR(32),
    status_after VARCHAR(32),
    details TEXT,
    correlation_id VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS retry_logs (
    id UUID PRIMARY KEY,
    job_id UUID REFERENCES embedding_jobs(id) ON DELETE CASCADE,
    chunk_id UUID,
    attempt_number INTEGER NOT NULL CHECK (attempt_number > 0),
    retry_reason VARCHAR(128) NOT NULL,
    error_message TEXT,
    next_retry_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS failure_logs (
    id UUID PRIMARY KEY,
    job_id UUID REFERENCES embedding_jobs(id) ON DELETE SET NULL,
    document_id UUID,
    chunk_id UUID,
    failure_stage VARCHAR(64) NOT NULL,
    error_code VARCHAR(128),
    error_message TEXT,
    stack_trace TEXT,
    payload TEXT,
    permanent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_embedding_jobs_document_id ON embedding_jobs(document_id);
CREATE INDEX IF NOT EXISTS idx_embedding_jobs_status ON embedding_jobs(status);
CREATE INDEX IF NOT EXISTS idx_embedding_jobs_created_at ON embedding_jobs(created_at);

CREATE INDEX IF NOT EXISTS idx_embedding_status_document_id ON embedding_status(document_id);
CREATE INDEX IF NOT EXISTS idx_embedding_status_chunk_id ON embedding_status(chunk_id);
CREATE INDEX IF NOT EXISTS idx_embedding_status_status ON embedding_status(status);
CREATE INDEX IF NOT EXISTS idx_embedding_status_job_id ON embedding_status(job_id);

CREATE INDEX IF NOT EXISTS idx_embedding_audit_job_id ON embedding_audit(job_id);
CREATE INDEX IF NOT EXISTS idx_embedding_audit_document_id ON embedding_audit(document_id);
CREATE INDEX IF NOT EXISTS idx_embedding_audit_created_at ON embedding_audit(created_at);

CREATE INDEX IF NOT EXISTS idx_retry_logs_job_id ON retry_logs(job_id);
CREATE INDEX IF NOT EXISTS idx_retry_logs_chunk_id ON retry_logs(chunk_id);

CREATE INDEX IF NOT EXISTS idx_failure_logs_job_id ON failure_logs(job_id);
CREATE INDEX IF NOT EXISTS idx_failure_logs_document_id ON failure_logs(document_id);
CREATE INDEX IF NOT EXISTS idx_failure_logs_stage ON failure_logs(failure_stage);
