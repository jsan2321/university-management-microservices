package com.panadi.ums.auditservice;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name = "audit_records") class AuditRecord {
 @Id UUID eventId; String eventType; String producer; String aggregateType; UUID aggregateId; UUID actorId; Instant occurredAt; String traceId;
 @Column(columnDefinition = "jsonb") String payload;
 protected AuditRecord() { }
 AuditRecord(UUID id, String type, String producer, String aggregateType, UUID aggregateId, UUID actorId, Instant occurredAt, String traceId, String payload) { this.eventId=id; this.eventType=type; this.producer=producer; this.aggregateType=aggregateType; this.aggregateId=aggregateId; this.actorId=actorId; this.occurredAt=occurredAt; this.traceId=traceId; this.payload=payload; }
}
