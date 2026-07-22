package com.panadi.ums.assignmentservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Submission(
        UUID id,
        UUID assignmentId,
        UUID studentId,
        String content,
        SubmissionStatus status,
        BigDecimal score,
        String feedback,
        LocalDateTime submittedAt,
        LocalDateTime gradedAt,
        LocalDateTime gradeReleasedAt,
        LocalDateTime updatedAt
) {
    public Submission {
        Assignment.requireId(assignmentId, "Assignment id is required");
        Assignment.requireId(studentId, "Student id is required");
        if (content == null || content.isBlank()) throw new DomainValidationException("Submission content is required");
        if (status == null) throw new DomainValidationException("Submission status is required");
        if (score != null && score.signum() < 0) throw new DomainValidationException("Score cannot be negative");
    }

    public static Submission create(UUID assignmentId, UUID studentId, String content, LocalDateTime dueAt) {
        LocalDateTime now = LocalDateTime.now();
        SubmissionStatus status = now.isAfter(dueAt) ? SubmissionStatus.LATE : SubmissionStatus.ON_TIME;
        return new Submission(null, assignmentId, studentId, content.trim(), status, null, null, now, null, null, now);
    }

    public Submission grade(BigDecimal score, String feedback, BigDecimal maxPoints) {
        if (score == null) throw new DomainValidationException("Score is required");
        if (score.signum() < 0 || score.compareTo(maxPoints) > 0) {
            throw new DomainValidationException("Score must be between zero and the assignment maximum points");
        }
        LocalDateTime now = LocalDateTime.now();
        return new Submission(id, assignmentId, studentId, content, status, score, feedback, submittedAt, now, null, now);
    }

    public Submission releaseGrade() {
        if (score == null || gradedAt == null) throw new DomainValidationException("Submission must be graded before releasing the grade");
        if (gradeReleasedAt != null) throw new DomainValidationException("Grade is already released");
        LocalDateTime now = LocalDateTime.now();
        return new Submission(id, assignmentId, studentId, content, status, score, feedback, submittedAt, gradedAt, now, now);
    }

    public boolean gradeReleased() {
        return gradeReleasedAt != null;
    }
}
