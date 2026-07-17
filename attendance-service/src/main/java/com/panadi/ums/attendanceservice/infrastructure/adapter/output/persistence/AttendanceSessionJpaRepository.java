package com.panadi.ums.attendanceservice.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

interface AttendanceSessionJpaRepository extends JpaRepository<AttendanceSessionEntity, UUID>, JpaSpecificationExecutor<AttendanceSessionEntity> {
    boolean existsBySectionIdAndSessionNumber(UUID sectionId, int sessionNumber);
    long countBySectionId(UUID sectionId);
}
