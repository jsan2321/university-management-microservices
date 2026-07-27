create table audit_records (
 event_id uuid primary key, event_type varchar(100) not null, producer varchar(100) not null,
 aggregate_type varchar(100) not null, aggregate_id uuid not null, actor_id uuid, occurred_at timestamptz not null,
 trace_id varchar(64), payload jsonb not null
);
