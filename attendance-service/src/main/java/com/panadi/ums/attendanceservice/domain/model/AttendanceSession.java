package com.panadi.ums.attendanceservice.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AttendanceSession(
        UUID id,
        UUID sectionId,
        int sessionNumber,
        LocalDate date,
        String topic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public AttendanceSession {
        requireId(sectionId, "Section id is required");
        if (sessionNumber <= 0) {
            throw new DomainValidationException("Session number must be positive");
        }
        if (date == null) {
            throw new DomainValidationException("Session date is required");
        }
    }

    public static AttendanceSession create(UUID sectionId, int sessionNumber, LocalDate date, String topic) {
        LocalDateTime now = LocalDateTime.now();
        return new AttendanceSession(null, sectionId, sessionNumber, date, topic, now, now);
    }

    static void requireId(UUID id, String message) {
        if (id == null) {
            throw new DomainValidationException(message);
        }
    }
}
