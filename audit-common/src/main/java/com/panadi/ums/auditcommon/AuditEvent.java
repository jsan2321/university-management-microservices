package com.panadi.ums.auditcommon;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** PII-minimized, versioned contract for the internal audit topic. */
public record AuditEvent(UUID eventId, String eventType, int schemaVersion, String producer,
                         String aggregateType, UUID aggregateId, UUID actorId,
                         Instant occurredAt, String traceId, Map<String, Object> payload) {
    public static AuditEvent create(String type, String producer, String aggregateType, UUID aggregateId,
                                    UUID actorId, String traceId, Map<String, Object> payload) {
        return new AuditEvent(UUID.randomUUID(), type, 1, producer, aggregateType, aggregateId, actorId,
                Instant.now(), traceId, payload == null ? Map.of() : Map.copyOf(payload));
    }
}
