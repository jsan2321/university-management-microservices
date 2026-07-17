package com.panadi.ums.academicservice.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Teacher(
        UUID id,
        UUID departmentId,
        UUID userId,
        String teacherCode,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate hireDate,
        AcademicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Teacher {
        AcademicProgram.requireId(departmentId, "Department id is required");
        Department.requireText(teacherCode, "Teacher code is required");
        Department.requireText(firstName, "Teacher first name is required");
        Department.requireText(lastName, "Teacher last name is required");
        Department.requireText(email, "Teacher email is required");
        if (hireDate == null) {
            throw new DomainValidationException("Teacher hire date is required");
        }
        if (status == null) {
            throw new DomainValidationException("Teacher status is required");
        }
    }

    public static Teacher create(UUID departmentId, UUID userId, String teacherCode, String firstName, String lastName, String email, String phone, LocalDate hireDate) {
        LocalDateTime now = LocalDateTime.now();
        return new Teacher(null, departmentId, userId, teacherCode, firstName, lastName, email, phone, hireDate, AcademicStatus.ACTIVE, now, now);
    }

    public Teacher update(UUID departmentId, UUID userId, String teacherCode, String firstName, String lastName, String email, String phone, LocalDate hireDate) {
        return new Teacher(id, departmentId, userId, teacherCode, firstName, lastName, email, phone, hireDate, status, createdAt, LocalDateTime.now());
    }

    public Teacher activate() {
        return new Teacher(id, departmentId, userId, teacherCode, firstName, lastName, email, phone, hireDate, AcademicStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    public Teacher deactivate() {
        return new Teacher(id, departmentId, userId, teacherCode, firstName, lastName, email, phone, hireDate, AcademicStatus.INACTIVE, createdAt, LocalDateTime.now());
    }
}
