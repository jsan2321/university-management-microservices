package com.panadi.ums.auditservice;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
@Component class AuditConsumer {
 private final ObjectMapper json; private final AuditRecordRepository records;
 AuditConsumer(ObjectMapper json, AuditRecordRepository records) { this.json=json; this.records=records; }
 @KafkaListener(topics="ums.audit.v1", groupId="audit-service") @Transactional
 void consume(String value) throws Exception { JsonNode e=json.readTree(value); UUID id=UUID.fromString(e.get("eventId").asText()); if (records.existsById(id)) return;
   try { records.save(new AuditRecord(id,e.get("eventType").asText(),e.get("producer").asText(),e.get("aggregateType").asText(),UUID.fromString(e.get("aggregateId").asText()), e.hasNonNull("actorId")?UUID.fromString(e.get("actorId").asText()):null, Instant.parse(e.get("occurredAt").asText()),e.path("traceId").isNull()?null:e.path("traceId").asText(),value)); } catch (DataIntegrityViolationException ignored) { }
 }
}
