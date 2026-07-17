package com.panadi.ums.academicservice.domain.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record Subject(
        UUID id,
        UUID programId,
        String code,
        String name,
        String description,
        int credits,
        Integer minimumCreditsRequired,
        Set<UUID> prerequisiteSubjectIds,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Subject {
        AcademicProgram.requireId(programId, "Program id is required");
        Department.requireText(code, "Subject code is required");
        Department.requireText(name, "Subject name is required");
        if (credits < 1 || credits > 10) {
            throw new DomainValidationException("Subject credits must be between 1 and 10");
        }
        if (minimumCreditsRequired != null && minimumCreditsRequired < 0) {
            throw new DomainValidationException("Minimum credits required cannot be negative");
        }
        if (prerequisiteSubjectIds != null && id != null && prerequisiteSubjectIds.contains(id)) {
            throw new DomainValidationException("Subject cannot be its own prerequisite");
        }
        if (status == null) {
            throw new DomainValidationException("Subject status is required");
        }
        prerequisiteSubjectIds = prerequisiteSubjectIds == null ? Set.of() : Set.copyOf(prerequisiteSubjectIds);
    }

    public static Subject create(UUID programId, String code, String name, String description, int credits, Integer minimumCreditsRequired, Set<UUID> prerequisites) {
        LocalDateTime now = LocalDateTime.now();
        return new Subject(null, programId, code, name, description, credits, minimumCreditsRequired, prerequisites, AcademicStatus.ACTIVE, now, now);
    }

    public Subject update(UUID programId, String code, String name, String description, int credits, Integer minimumCreditsRequired, Set<UUID> prerequisites) {
        return new Subject(id, programId, code, name, description, credits, minimumCreditsRequired, prerequisites, status, createdAt, LocalDateTime.now());
    }

    public Subject activate() {
        return new Subject(id, programId, code, name, description, credits, minimumCreditsRequired, prerequisiteSubjectIds, AcademicStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    public Subject deactivate() {
        return new Subject(id, programId, code, name, description, credits, minimumCreditsRequired, prerequisiteSubjectIds, AcademicStatus.INACTIVE, createdAt, LocalDateTime.now());
    }
}
