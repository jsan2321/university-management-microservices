CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE attendance_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id UUID NOT NULL,
    session_number INTEGER NOT NULL,
    date DATE NOT NULL,
    topic VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_attendance_session_number UNIQUE(section_id, session_number)
);

CREATE INDEX idx_attendance_sessions_section_id ON attendance_sessions(section_id);
CREATE INDEX idx_attendance_sessions_date ON attendance_sessions(date);
