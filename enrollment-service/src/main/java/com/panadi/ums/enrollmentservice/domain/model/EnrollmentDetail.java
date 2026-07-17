package com.panadi.ums.enrollmentservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnrollmentDetail(
        UUID id,
        UUID sectionId,
        UUID subjectId,
        int credits,
        LocalDateTime createdAt
) {
    public EnrollmentDetail {
        requireId(sectionId, "Section id is required");
        requireId(subjectId, "Subject id is required");
        if (credits <= 0) {
            throw new DomainValidationException("Credits must be positive");
        }
    }

    public static EnrollmentDetail create(UUID sectionId, UUID subjectId, int credits) {
        return new EnrollmentDetail(null, sectionId, subjectId, credits, LocalDateTime.now());
    }

    static void requireId(UUID id, String message) {
        if (id == null) {
            throw new DomainValidationException(message);
        }
    }
}
