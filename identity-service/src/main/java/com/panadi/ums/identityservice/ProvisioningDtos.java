package com.panadi.ums.identityservice;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

final class ProvisioningDtos {
    private ProvisioningDtos() {}

    record ProvisionTeacherRequest(
            @NotBlank String username,
            @Email @NotBlank String email,
            @Size(min = 8) @NotBlank String temporaryPassword,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotNull UUID departmentId,
            @NotBlank String teacherCode,
            String phone,
            @NotNull LocalDate hireDate
    ) {}

    record ProvisionStudentRequest(
            @NotBlank String username,
            @Email @NotBlank String email,
            @Size(min = 8) @NotBlank String temporaryPassword,
            @NotBlank String firstName,
            @NotBlank String lastName,
            String gender,
            @NotNull LocalDate dateOfBirth,
            @NotBlank String studentCode,
            String phone,
            String address,
            @NotNull UUID programId,
            @NotNull LocalDate admissionDate
    ) {}

    record LinkExistingRequest(@NotNull UUID userId, @NotNull UUID profileId) {}

    record ProvisioningResponse(UUID provisioningId, UUID userId, UUID profileId, String role, String status) {}
    record ErrorResponse(String code, String message) {}
}
