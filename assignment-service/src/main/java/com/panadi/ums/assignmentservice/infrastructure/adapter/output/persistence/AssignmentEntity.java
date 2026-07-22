package com.panadi.ums.assignmentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.assignmentservice.domain.model.AssignmentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignments")
class AssignmentEntity {
    @Id UUID id;
    UUID sectionId;
    UUID teacherId;
    String title;
    String description;
    LocalDateTime dueAt;
    BigDecimal maxPoints;
    @Enumerated(EnumType.STRING) AssignmentStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime publishedAt;
    LocalDateTime closedAt;
}
