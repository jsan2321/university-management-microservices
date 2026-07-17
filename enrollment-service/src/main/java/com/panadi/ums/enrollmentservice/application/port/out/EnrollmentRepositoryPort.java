package com.panadi.ums.enrollmentservice.application.port.out;

import com.panadi.ums.enrollmentservice.application.PageResult;
import com.panadi.ums.enrollmentservice.domain.model.Enrollment;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepositoryPort {
    Enrollment saveEnrollment(Enrollment enrollment);
    Optional<Enrollment> findEnrollmentById(UUID id);
    PageResult<Enrollment> findEnrollments(UUID studentId, UUID semesterId, EnrollmentStatus status, int page, int size);
    boolean existsActiveEnrollment(UUID studentId, UUID semesterId);
    long countActiveEnrollmentDetailsBySectionId(UUID sectionId);
    List<UUID> findActiveStudentIdsBySectionId(UUID sectionId);
}
