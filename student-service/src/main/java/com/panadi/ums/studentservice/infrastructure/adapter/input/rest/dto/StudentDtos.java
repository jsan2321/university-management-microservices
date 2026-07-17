package com.panadi.ums.studentservice.infrastructure.adapter.input.rest.dto;

import com.panadi.ums.studentservice.domain.model.Gender;
import com.panadi.ums.studentservice.domain.model.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class StudentDtos {
    private StudentDtos() {
    }

    public record StudentRequest(
            UUID userId,
            @NotBlank String studentCode,
            @NotBlank String firstName,
            @NotBlank String lastName,
            Gender gender,
            @NotNull LocalDate dateOfBirth,
            @Email @NotBlank String email,
            String phone,
            String address,
            @NotNull UUID programId,
            @NotNull LocalDate admissionDate
    ) {
    }

    public record StudentResponse(
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
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    public record ErrorResponse(String code, String message, LocalDateTime timestamp) {
    }
}
