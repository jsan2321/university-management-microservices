package com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto;

import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class AttendanceDtos {
    private AttendanceDtos() {
    }

    public record CreateAttendanceSessionRequest(@NotNull UUID sectionId, @Positive int sessionNumber, @NotNull LocalDate date, String topic) {
    }

    public record RecordAttendanceRequest(@NotEmpty List<@Valid AttendanceRecordRequest> records) {
    }

    public record AttendanceRecordRequest(@NotNull UUID studentId, @NotNull AttendanceStatus status) {
    }

    public record AttendanceSessionResponse(UUID id, UUID sectionId, int sessionNumber, LocalDate date, String topic, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record AttendanceResponse(UUID id, UUID attendanceSessionId, UUID studentId, AttendanceStatus status, LocalDateTime recordedAt, LocalDateTime updatedAt) {
    }

    public record AttendancePercentageResponse(UUID studentId, UUID sectionId, long presentCount, long totalSessions, double percentage, boolean eligibleForFinalEvaluation) {
    }

    public record SectionRosterResponse(UUID sectionId, List<RosterStudentResponse> students) {
    }

    public record RosterStudentResponse(UUID studentId, String studentCode, String firstName, String lastName) {
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    public record ErrorResponse(String code, String message, LocalDateTime timestamp) {
    }
}
