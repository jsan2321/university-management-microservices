package com.panadi.ums.auditcommon;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("ums.audit.outbox")
public record AuditOutboxProperties(boolean enabled) { }
