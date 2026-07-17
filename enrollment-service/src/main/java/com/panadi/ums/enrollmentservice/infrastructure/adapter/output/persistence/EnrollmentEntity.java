package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
class EnrollmentEntity {
    @Id
    UUID id;
    UUID studentId;
    UUID semesterId;
    @Enumerated(EnumType.STRING)
    EnrollmentStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime cancelledAt;
    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EnrollmentDetailEntity> details = new ArrayList<>();
}
