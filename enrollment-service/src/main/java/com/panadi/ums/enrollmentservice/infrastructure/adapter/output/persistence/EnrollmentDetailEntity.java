package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollment_details")
class EnrollmentDetailEntity {
    @Id
    UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "enrollment_id")
    EnrollmentEntity enrollment;
    UUID sectionId;
    UUID subjectId;
    int credits;
    LocalDateTime createdAt;
}
