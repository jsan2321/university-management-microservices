package com.panadi.ums.auditcommon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuditOutbox {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final KafkaTemplate kafka;
    private final Tracer tracer;
    private final Counter failures;

    AuditOutbox(JdbcTemplate jdbc, ObjectMapper json, KafkaTemplate kafka, Tracer tracer,
                AuditOutboxProperties properties, MeterRegistry meters) {
        this.jdbc = jdbc; this.json = json; this.kafka = kafka; this.tracer = tracer;
        this.failures = meters != null ? Counter.builder("ums.audit.outbox.publish.failures").register(meters) : null;
    }

    public void record(String type, String producer, String aggregateType, UUID aggregateId, UUID actorId, Map<String, Object> payload) {
        String traceId = (tracer == null || tracer.currentSpan() == null) ? null : tracer.currentSpan().context().traceId();
        AuditEvent event = AuditEvent.create(type, producer, aggregateType, aggregateId, actorId, traceId, payload);
        try {
            jdbc.update("insert into audit_outbox (event_id, event_type, payload, occurred_at) values (cast(? as uuid), ?, cast(? as jsonb), ?)",
                    event.eventId().toString(), event.eventType(), json.writeValueAsString(event), java.sql.Timestamp.from(event.occurredAt()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize audit event", exception);
        }
    }

    @Scheduled(fixedDelayString = "${ums.audit.outbox.delay:1000}")
    @Transactional
    void publishPending() {
        List<OutboxRow> rows = jdbc.query("select event_id, payload from audit_outbox where published_at is null order by occurred_at limit 100 for update skip locked",
                (ResultSet rs, int ignored) -> new OutboxRow(UUID.fromString(rs.getString("event_id")), rs.getString("payload")));
        for (OutboxRow row : rows) {
            try {
                kafka.send("ums.audit.v1", row.id().toString(), row.payload()).get();
                jdbc.update("update audit_outbox set published_at = ?, publish_attempts = publish_attempts + 1, last_error = null where event_id = cast(? as uuid)", java.sql.Timestamp.from(Instant.now()), row.id().toString());
            } catch (Exception exception) {
                if (failures != null) failures.increment();
                jdbc.update("update audit_outbox set publish_attempts = publish_attempts + 1, last_error = ? where event_id = cast(? as uuid)", exception.getMessage(), row.id().toString());
            }
        }
    }
    private record OutboxRow(UUID id, String payload) { }
}
