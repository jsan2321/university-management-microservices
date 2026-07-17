package com.panadi.ums.studentservice.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

public record Student(
        UUID id,
        UUID userId,
        String studentCode,
        String firstName,
        String lastName,
        Gender gender,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String address,
        UUID programId,
        LocalDate admissionDate,
        StudentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    private static final int MINIMUM_AGE = 17;

    public Student {
        requireText(studentCode, "Student code is required");
        requireText(firstName, "Student first name is required");
        requireText(lastName, "Student last name is required");
        requireText(email, "Student email is required");
        requireId(programId, "Program id is required");
        if (dateOfBirth == null) {
            throw new DomainValidationException("Student date of birth is required");
        }
        if (admissionDate == null) {
            throw new DomainValidationException("Student admission date is required");
        }
        if (status == null) {
            throw new DomainValidationException("Student status is required");
        }
        if (Period.between(dateOfBirth, LocalDate.now()).getYears() < MINIMUM_AGE) {
            throw new DomainValidationException("Student must be at least 17 years old");
        }
        gender = gender == null ? Gender.UNSPECIFIED : gender;
    }

    public static Student create(
            UUID userId,
            String studentCode,
            String firstName,
            String lastName,
            Gender gender,
            LocalDate dateOfBirth,
            String email,
            String phone,
            String address,
            UUID programId,
            LocalDate admissionDate
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Student(null, userId, studentCode, firstName, lastName, gender, dateOfBirth, email, phone, address, programId, admissionDate, StudentStatus.ACTIVE, now, now);
    }

    public Student update(
            UUID userId,
            String studentCode,
            String firstName,
            String lastName,
            Gender gender,
            LocalDate dateOfBirth,
            String email,
            String phone,
            String address,
            UUID programId,
            LocalDate admissionDate
    ) {
        return new Student(id, userId, studentCode, firstName, lastName, gender, dateOfBirth, email, phone, address, programId, admissionDate, status, createdAt, LocalDateTime.now());
    }

    public Student activate() {
        return new Student(id, userId, studentCode, firstName, lastName, gender, dateOfBirth, email, phone, address, programId, admissionDate, StudentStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    public Student deactivate() {
        return new Student(id, userId, studentCode, firstName, lastName, gender, dateOfBirth, email, phone, address, programId, admissionDate, StudentStatus.INACTIVE, createdAt, LocalDateTime.now());
    }

    public Student suspend() {
        return new Student(id, userId, studentCode, firstName, lastName, gender, dateOfBirth, email, phone, address, programId, admissionDate, StudentStatus.SUSPENDED, createdAt, LocalDateTime.now());
    }

    static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
    }

    static void requireId(UUID id, String message) {
        if (id == null) {
            throw new DomainValidationException(message);
        }
    }
}
