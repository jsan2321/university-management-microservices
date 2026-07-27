CREATE TABLE identifier_sequences (
    sequence_key VARCHAR(32) PRIMARY KEY,
    next_value BIGINT NOT NULL
);
