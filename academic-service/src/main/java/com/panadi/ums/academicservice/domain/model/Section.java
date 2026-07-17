package com.panadi.ums.academicservice.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Section(
        UUID id,
        UUID subjectId,
        UUID teacherId,
        UUID semesterId,
        String sectionCode,
        int capacity,
        List<SectionSchedule> schedules,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Section {
        AcademicProgram.requireId(subjectId, "Subject id is required");
        AcademicProgram.requireId(teacherId, "Teacher id is required");
        AcademicProgram.requireId(semesterId, "Semester id is required");
        Department.requireText(sectionCode, "Section code is required");
        AcademicProgram.requirePositive(capacity, "Section capacity must be positive");
        if (status == null) {
            throw new DomainValidationException("Section status is required");
        }
        schedules = schedules == null ? List.of() : List.copyOf(schedules);
    }

    public static Section create(UUID subjectId, UUID teacherId, UUID semesterId, String sectionCode, int capacity, List<SectionSchedule> schedules) {
        LocalDateTime now = LocalDateTime.now();
        return new Section(null, subjectId, teacherId, semesterId, sectionCode, capacity, schedules, AcademicStatus.ACTIVE, now, now);
    }

    public Section update(UUID subjectId, UUID teacherId, UUID semesterId, String sectionCode, int capacity, List<SectionSchedule> schedules) {
        return new Section(id, subjectId, teacherId, semesterId, sectionCode, capacity, schedules, status, createdAt, LocalDateTime.now());
    }

    public Section activate() {
        return new Section(id, subjectId, teacherId, semesterId, sectionCode, capacity, schedules, AcademicStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    public Section deactivate() {
        return new Section(id, subjectId, teacherId, semesterId, sectionCode, capacity, schedules, AcademicStatus.INACTIVE, createdAt, LocalDateTime.now());
    }
}
