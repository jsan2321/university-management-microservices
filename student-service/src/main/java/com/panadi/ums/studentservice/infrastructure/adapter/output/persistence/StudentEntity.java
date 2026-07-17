package com.panadi.ums.studentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.studentservice.domain.model.Gender;
import com.panadi.ums.studentservice.domain.model.StudentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "students")
class StudentEntity {
    @Id
    UUID id;
    UUID userId;
    String studentCode;
    String firstName;
    String lastName;
    @Enumerated(EnumType.STRING)
    Gender gender;
    LocalDate dateOfBirth;
    String email;
    String phone;
    String address;
    UUID programId;
    LocalDate admissionDate;
    @Enumerated(EnumType.STRING)
    StudentStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
