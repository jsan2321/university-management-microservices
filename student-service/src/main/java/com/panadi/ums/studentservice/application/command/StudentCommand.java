package com.panadi.ums.studentservice.application.command;

import com.panadi.ums.studentservice.domain.model.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record StudentCommand(
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
}
