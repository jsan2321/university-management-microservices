package com.panadi.ums.assignmentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.assignmentservice.domain.model.SubmissionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions")
class SubmissionEntity {
    @Id UUID id;
    UUID assignmentId;
    UUID studentId;
    String content;
    @Enumerated(EnumType.STRING) SubmissionStatus status;
    BigDecimal score;
    String feedback;
    LocalDateTime submittedAt;
    LocalDateTime gradedAt;
    LocalDateTime gradeReleasedAt;
    LocalDateTime updatedAt;
}
