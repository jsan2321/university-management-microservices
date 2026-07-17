package com.panadi.ums.academicservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record AcademicProgram(
        UUID id,
        UUID departmentId,
        String code,
        String name,
        int durationSemesters,
        int totalCredits,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public AcademicProgram {
        requireId(departmentId, "Department id is required");
        Department.requireText(code, "Program code is required");
        Department.requireText(name, "Program name is required");
        requirePositive(durationSemesters, "Program duration must be positive");
        requirePositive(totalCredits, "Program total credits must be positive");
        if (status == null) {
            throw new DomainValidationException("Program status is required");
        }
    }

    public static AcademicProgram create(UUID departmentId, String code, String name, int durationSemesters, int totalCredits) {
        LocalDateTime now = LocalDateTime.now();
        return new AcademicProgram(null, departmentId, code, name, durationSemesters, totalCredits, AcademicStatus.ACTIVE, now, now);
    }

    public AcademicProgram update(UUID departmentId, String code, String name, int durationSemesters, int totalCredits) {
        return new AcademicProgram(id, departmentId, code, name, durationSemesters, totalCredits, status, createdAt, LocalDateTime.now());
    }

    public AcademicProgram activate() {
        return new AcademicProgram(id, departmentId, code, name, durationSemesters, totalCredits, AcademicStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    public AcademicProgram deactivate() {
        return new AcademicProgram(id, departmentId, code, name, durationSemesters, totalCredits, AcademicStatus.INACTIVE, createdAt, LocalDateTime.now());
    }

    static void requireId(UUID id, String message) {
        if (id == null) {
            throw new DomainValidationException(message);
        }
    }

    static void requirePositive(int value, String message) {
        if (value <= 0) {
            throw new DomainValidationException(message);
        }
    }
}
