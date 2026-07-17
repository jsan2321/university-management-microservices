package com.panadi.ums.academicservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Department(
        UUID id,
        String code,
        String name,
        String description,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Department {
        requireText(code, "Department code is required");
        requireText(name, "Department name is required");
        if (status == null) {
            throw new DomainValidationException("Department status is required");
        }
    }

    public static Department create(String code, String name, String description) {
        LocalDateTime now = LocalDateTime.now();
        return new Department(null, code, name, description, AcademicStatus.ACTIVE, now, now);
    }

    public Department update(String code, String name, String description) {
        return new Department(id, code, name, description, status, createdAt, LocalDateTime.now());
    }

    public Department activate() {
        return new Department(id, code, name, description, AcademicStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    public Department deactivate() {
        return new Department(id, code, name, description, AcademicStatus.INACTIVE, createdAt, LocalDateTime.now());
    }

    static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
    }
}
