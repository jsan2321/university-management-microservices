create table audit_outbox (
    event_id uuid primary key, event_type varchar(100) not null, payload jsonb not null,
    occurred_at timestamptz not null, published_at timestamptz, publish_attempts integer not null default 0, last_error text
);
create index ix_audit_outbox_pending on audit_outbox (occurred_at) where published_at is null;
