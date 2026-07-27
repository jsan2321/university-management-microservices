package com.panadi.ums.auditcommon;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(AuditOutboxProperties.class)
public class AuditOutboxConfiguration {
    @Bean
    @ConditionalOnProperty(name = "ums.audit.outbox.enabled", havingValue = "true")
    AuditOutbox auditOutbox(JdbcTemplate jdbc, ObjectMapper json, KafkaTemplate<String, String> kafka,
                                  Tracer tracer, AuditOutboxProperties properties, MeterRegistry meters) {
        return new AuditOutbox(jdbc, json, kafka, tracer, properties, meters);
    }
}
