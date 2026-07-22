package com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto;

import com.panadi.ums.assignmentservice.domain.model.AssignmentStatus;
import com.panadi.ums.assignmentservice.domain.model.SubmissionStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class AssignmentDtos {
    private AssignmentDtos() { }

    public record CreateAssignmentRequest(@NotNull UUID sectionId, UUID teacherId, @NotBlank String title, String description, @Future @NotNull LocalDateTime dueAt, @Positive BigDecimal maxPoints) { }
    public record TeacherActionRequest(UUID teacherId) { }
    public record SubmitAssignmentRequest(UUID studentId, @NotBlank String content) { }
    public record GradeSubmissionRequest(UUID teacherId, @NotNull @DecimalMin("0.0") BigDecimal score, String feedback) { }

    public record AssignmentResponse(UUID id, UUID sectionId, UUID teacherId, String title, String description, LocalDateTime dueAt, BigDecimal maxPoints, AssignmentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime publishedAt, LocalDateTime closedAt) { }
    public record SubmissionResponse(UUID id, UUID assignmentId, UUID studentId, String content, SubmissionStatus status, BigDecimal score, String feedback, boolean gradeReleased, LocalDateTime submittedAt, LocalDateTime gradedAt, LocalDateTime gradeReleasedAt, LocalDateTime updatedAt) { }
    public record GradedSubmissionResponse(UUID id, UUID assignmentId, UUID studentId, String content, SubmissionStatus status, BigDecimal score, String feedback, boolean gradeReleased, LocalDateTime submittedAt, LocalDateTime gradedAt, LocalDateTime gradeReleasedAt, LocalDateTime updatedAt) { }
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) { }
    public record ErrorResponse(String code, String message, LocalDateTime timestamp) { }
}
