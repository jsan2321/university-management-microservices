package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

interface EnrollmentJpaRepository extends JpaRepository<EnrollmentEntity, UUID>, JpaSpecificationExecutor<EnrollmentEntity> {
    boolean existsByStudentIdAndSemesterIdAndStatus(UUID studentId, UUID semesterId, EnrollmentStatus status);
}
