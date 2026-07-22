CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE provisioning_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(200) NOT NULL UNIQUE,
    profile_type VARCHAR(20) NOT NULL,
    user_id UUID,
    profile_id UUID,
    status VARCHAR(40) NOT NULL,
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_provisioning_user_id ON provisioning_requests(user_id);
CREATE INDEX idx_provisioning_status ON provisioning_requests(status);
