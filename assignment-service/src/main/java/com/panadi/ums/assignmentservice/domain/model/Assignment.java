package com.panadi.ums.assignmentservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Assignment(
        UUID id,
        UUID sectionId,
        UUID teacherId,
        String title,
        String description,
        LocalDateTime dueAt,
        BigDecimal maxPoints,
        AssignmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        LocalDateTime closedAt
) {
    public Assignment {
        requireId(sectionId, "Section id is required");
        requireId(teacherId, "Teacher id is required");
        if (title == null || title.isBlank()) throw new DomainValidationException("Title is required");
        if (dueAt == null) throw new DomainValidationException("Due date is required");
        if (maxPoints == null || maxPoints.signum() <= 0) throw new DomainValidationException("Maximum points must be positive");
        if (status == null) throw new DomainValidationException("Assignment status is required");
        if (createdAt != null && !dueAt.isAfter(createdAt)) throw new DomainValidationException("Due date must be after creation date");
    }

    public static Assignment create(UUID sectionId, UUID teacherId, String title, String description, LocalDateTime dueAt, BigDecimal maxPoints) {
        LocalDateTime now = LocalDateTime.now();
        return new Assignment(null, sectionId, teacherId, title.trim(), description, dueAt, maxPoints, AssignmentStatus.DRAFT, now, now, null, null);
    }

    public Assignment publish() {
        if (status != AssignmentStatus.DRAFT) throw new DomainValidationException("Only draft assignments can be published");
        LocalDateTime now = LocalDateTime.now();
        return new Assignment(id, sectionId, teacherId, title, description, dueAt, maxPoints, AssignmentStatus.PUBLISHED, createdAt, now, now, null);
    }

    public Assignment close() {
        if (status != AssignmentStatus.PUBLISHED) throw new DomainValidationException("Only published assignments can be closed");
        LocalDateTime now = LocalDateTime.now();
        return new Assignment(id, sectionId, teacherId, title, description, dueAt, maxPoints, AssignmentStatus.CLOSED, createdAt, now, publishedAt, now);
    }

    public void verifyTeacher(UUID candidateTeacherId) {
        requireId(candidateTeacherId, "Teacher id is required");
        if (!teacherId.equals(candidateTeacherId)) throw new DomainValidationException("Teacher is not assigned to this assignment's section");
    }

    static void requireId(UUID id, String message) {
        if (id == null) throw new DomainValidationException(message);
    }
}
