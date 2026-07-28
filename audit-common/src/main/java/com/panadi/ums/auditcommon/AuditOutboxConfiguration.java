package com.panadi.ums.auditcommon;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.UUID;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(AuditOutboxProperties.class)
public class AuditOutboxConfiguration {

    @Bean
    @ConditionalOnProperty(name = "ums.audit.outbox.enabled", havingValue = "true")
    AuditOutbox realAuditOutbox(
            JdbcTemplate jdbc,
            ObjectMapper json,
            KafkaTemplate<String, String> kafka,
            Tracer tracer,
            AuditOutboxProperties properties,
            MeterRegistry meters
    ) {
        return new AuditOutbox(jdbc, json, kafka, tracer, properties, meters);
    }

    @Bean
    @ConditionalOnMissingBean(AuditOutbox.class)
    AuditOutbox noOpAuditOutbox() {
        return new NoOpAuditOutbox();
    }

    private static class NoOpAuditOutbox extends AuditOutbox {
        public NoOpAuditOutbox() {
            super(
                null, null, null, null,
                new AuditOutboxProperties(false),
                null
            );
        }

        @Override
        public void record(String type, String producer, String aggregateType,
                           UUID aggregateId, UUID actorId, Map<String, Object> payload) {
            // no-op
        }

        @Override
        void publishPending() {
            // no-op
        }
    }
}
