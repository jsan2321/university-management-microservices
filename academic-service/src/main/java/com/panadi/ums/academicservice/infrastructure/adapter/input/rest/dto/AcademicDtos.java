package com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto;

import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.SemesterStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AcademicDtos {
    private AcademicDtos() {
    }

    public record DepartmentRequest(@NotBlank String code, @NotBlank String name, String description) {
    }

    public record DepartmentResponse(UUID id, String code, String name, String description, AcademicStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record ProgramRequest(@NotNull UUID departmentId, @NotBlank String code, @NotBlank String name, @Positive int durationSemesters, @Positive int totalCredits) {
    }

    public record ProgramResponse(UUID id, UUID departmentId, String code, String name, int durationSemesters, int totalCredits, AcademicStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record TeacherRequest(@NotNull UUID departmentId, UUID userId, @NotBlank String teacherCode, @NotBlank String firstName, @NotBlank String lastName, @Email @NotBlank String email, String phone, @NotNull LocalDate hireDate) {
    }

    public record TeacherResponse(UUID id, UUID departmentId, UUID userId, String teacherCode, String firstName, String lastName, String email, String phone, LocalDate hireDate, AcademicStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record SemesterRequest(@NotBlank String name, @NotNull LocalDate startDate, @FutureOrPresent @NotNull LocalDate endDate) {
    }

    public record SemesterResponse(UUID id, String name, LocalDate startDate, LocalDate endDate, SemesterStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record SubjectRequest(@NotNull UUID programId, @NotBlank String code, @NotBlank String name, String description, @Min(1) @Max(10) int credits, @Min(0) Integer minimumCreditsRequired, Set<UUID> prerequisiteSubjectIds) {
    }

    public record SubjectResponse(UUID id, UUID programId, String code, String name, String description, int credits, Integer minimumCreditsRequired, Set<UUID> prerequisiteSubjectIds, AcademicStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record SectionRequest(@NotNull UUID subjectId, @NotNull UUID teacherId, @NotNull UUID semesterId, @NotBlank String sectionCode, @Positive int capacity, List<@Valid ScheduleRequest> schedules) {
    }

    public record ScheduleRequest(@NotNull DayOfWeek dayOfWeek, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {
    }

    public record SectionResponse(UUID id, UUID subjectId, UUID teacherId, UUID semesterId, String sectionCode, int capacity, List<ScheduleResponse> schedules, AcademicStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record ScheduleResponse(UUID id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    public record ErrorResponse(String code, String message, LocalDateTime timestamp) {
    }
}
