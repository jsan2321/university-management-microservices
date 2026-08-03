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
            @Email @NotBlank String contactEmail,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotNull UUID departmentId,
            String phone,
            @NotNull LocalDate hireDate
    ) {}

    record ProvisionStudentRequest(
            @Email @NotBlank String contactEmail,
            @NotBlank String firstName,
            @NotBlank String lastName,
            String gender,
            @NotNull LocalDate dateOfBirth,
            String phone,
            String address,
            @NotNull UUID programId,
            @NotNull LocalDate admissionDate
    ) {}

    record LinkExistingRequest(@NotNull UUID userId, @NotNull UUID profileId) {}

    record ProvisioningResponse(UUID provisioningId, UUID userId, UUID profileId, String role, String status,
                                String academicCode, String username, String universityEmail) {}
    record ErrorResponse(String code, String message) {}
}
