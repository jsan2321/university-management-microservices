package com.panadi.ums.auditservice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
interface AuditRecordRepository extends JpaRepository<AuditRecord, UUID> { }
