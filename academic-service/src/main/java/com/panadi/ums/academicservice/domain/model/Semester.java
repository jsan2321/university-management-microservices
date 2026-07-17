package com.panadi.ums.academicservice.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Semester(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SemesterStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Semester {
        Department.requireText(name, "Semester name is required");
        if (startDate == null || endDate == null) {
            throw new DomainValidationException("Semester dates are required");
        }
        if (!startDate.isBefore(endDate)) {
            throw new DomainValidationException("Semester start date must be before end date");
        }
        if (status == null) {
            throw new DomainValidationException("Semester status is required");
        }
    }

    public static Semester create(String name, LocalDate startDate, LocalDate endDate) {
        LocalDateTime now = LocalDateTime.now();
        return new Semester(null, name, startDate, endDate, SemesterStatus.INACTIVE, now, now);
    }

    public Semester update(String name, LocalDate startDate, LocalDate endDate) {
        return new Semester(id, name, startDate, endDate, status, createdAt, LocalDateTime.now());
    }

    public Semester activate() {
        return new Semester(id, name, startDate, endDate, SemesterStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    public Semester deactivate() {
        return new Semester(id, name, startDate, endDate, SemesterStatus.INACTIVE, createdAt, LocalDateTime.now());
    }

    public Semester close() {
        return new Semester(id, name, startDate, endDate, SemesterStatus.CLOSED, createdAt, LocalDateTime.now());
    }
}
