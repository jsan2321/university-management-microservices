package com.panadi.ums.assignmentservice.application.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class AssignmentCommands {
    private AssignmentCommands() { }

    public record CreateAssignment(UUID sectionId, UUID teacherId, String title, String description, LocalDateTime dueAt, BigDecimal maxPoints) { }
    public record TeacherAction(UUID teacherId) { }
    public record SubmitAssignment(UUID studentId, String content) { }
    public record GradeSubmission(UUID teacherId, BigDecimal score, String feedback) { }
}
