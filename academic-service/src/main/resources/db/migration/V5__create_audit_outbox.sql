create table audit_outbox (
    event_id uuid primary key,
    event_type varchar(255) not null,
    payload jsonb not null,
    occurred_at timestamp not null,
    published_at timestamp,
    publish_attempts int default 0,
    last_error text
);

create index ix_audit_outbox_pending on audit_outbox (occurred_at) where published_at is null;
