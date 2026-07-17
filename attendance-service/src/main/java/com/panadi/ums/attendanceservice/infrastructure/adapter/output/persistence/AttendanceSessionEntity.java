package com.panadi.ums.attendanceservice.infrastructure.adapter.output.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_sessions")
class AttendanceSessionEntity {
    @Id
    UUID id;
    UUID sectionId;
    int sessionNumber;
    LocalDate date;
    String topic;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
