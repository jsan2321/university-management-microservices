package com.panadi.ums.attendanceservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendances")
class AttendanceEntity {
    @Id
    UUID id;
    UUID attendanceSessionId;
    UUID studentId;
    @Enumerated(EnumType.STRING)
    AttendanceStatus status;
    LocalDateTime recordedAt;
    LocalDateTime updatedAt;
}
