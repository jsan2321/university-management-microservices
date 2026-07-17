package com.panadi.ums.academicservice.application.command;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Commands {
    private Commands() {
    }

    public record DepartmentCommand(String code, String name, String description) {
    }

    public record ProgramCommand(UUID departmentId, String code, String name, int durationSemesters, int totalCredits) {
    }

    public record TeacherCommand(UUID departmentId, UUID userId, String teacherCode, String firstName, String lastName, String email, String phone, LocalDate hireDate) {
    }

    public record SemesterCommand(String name, LocalDate startDate, LocalDate endDate) {
    }

    public record SubjectCommand(UUID programId, String code, String name, String description, int credits, Integer minimumCreditsRequired, Set<UUID> prerequisiteSubjectIds) {
    }

    public record SectionCommand(UUID subjectId, UUID teacherId, UUID semesterId, String sectionCode, int capacity, List<ScheduleCommand> schedules) {
    }

    public record ScheduleCommand(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    }
}
