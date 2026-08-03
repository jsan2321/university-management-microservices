package com.panadi.ums.auditservice;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_records")
public class AuditRecord {
    @Id
    private UUID eventId;
    private String eventType;
    private String producer;
    private String aggregateType;
    private UUID aggregateId;
    private UUID actorId;
    private Instant occurredAt;
    private String traceId;

    @Column(columnDefinition = "jsonb")
    private String payload;

    protected AuditRecord() { }

    public AuditRecord(UUID eventId, String eventType, String producer, String aggregateType, UUID aggregateId, UUID actorId, Instant occurredAt, String traceId, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.producer = producer;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.actorId = actorId;
        this.occurredAt = occurredAt;
        this.traceId = traceId;
        this.payload = payload;
    }

    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getProducer() { return producer; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public UUID getActorId() { return actorId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getTraceId() { return traceId; }
    public String getPayload() { return payload; }
}
