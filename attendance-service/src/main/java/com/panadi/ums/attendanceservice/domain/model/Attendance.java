package com.panadi.ums.attendanceservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Attendance(
        UUID id,
        UUID attendanceSessionId,
        UUID studentId,
        AttendanceStatus status,
        LocalDateTime recordedAt,
        LocalDateTime updatedAt
) {
    public Attendance {
        AttendanceSession.requireId(attendanceSessionId, "Attendance session id is required");
        AttendanceSession.requireId(studentId, "Student id is required");
        if (status == null) {
            throw new DomainValidationException("Attendance status is required");
        }
    }

    public static Attendance create(UUID attendanceSessionId, UUID studentId, AttendanceStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new Attendance(null, attendanceSessionId, studentId, status, now, now);
    }

    public Attendance updateStatus(AttendanceStatus status) {
        return new Attendance(id, attendanceSessionId, studentId, status, recordedAt, LocalDateTime.now());
    }
}
