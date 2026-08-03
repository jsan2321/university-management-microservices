package com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto;

import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class EnrollmentDtos {
    private EnrollmentDtos() {
    }

    public record CreateEnrollmentRequest(@NotNull UUID studentId, @NotNull UUID semesterId, @NotEmpty List<UUID> sectionIds) {
    }
    public record AddSectionRequest(@NotNull UUID sectionId) {}

    public record EnrollmentResponse(UUID id, UUID studentId, UUID semesterId, SemesterSummaryResponse semester, EnrollmentStatus status, int totalCredits, List<EnrollmentDetailResponse> details, boolean isRegistrationOpen, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime cancelledAt) {
    }

    public record EnrollmentDetailResponse(UUID id, UUID sectionId, UUID subjectId, int credits, SectionSummaryResponse section, SubjectSummaryResponse subject) {
    }

    public record SemesterSummaryResponse(UUID id, String name) {}
    public record SectionSummaryResponse(UUID id, String sectionCode) {}
    public record SubjectSummaryResponse(UUID id, String code, String name) {}

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    public record SectionStudentsResponse(UUID sectionId, List<UUID> studentIds) {
    }

    public record ErrorResponse(String code, String message, LocalDateTime timestamp) {
    }
}
