package com.panadi.ums.enrollmentservice.application.port.in;

import com.panadi.ums.enrollmentservice.application.PageResult;
import com.panadi.ums.enrollmentservice.application.command.CreateEnrollmentCommand;
import com.panadi.ums.enrollmentservice.domain.model.Enrollment;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;

import java.util.List;
import java.util.UUID;

public interface EnrollmentUseCase {
    Enrollment createEnrollment(CreateEnrollmentCommand command);
    Enrollment getEnrollment(UUID id);
    PageResult<Enrollment> listEnrollments(UUID studentId, UUID semesterId, EnrollmentStatus status, int page, int size);
    List<UUID> listActiveStudentIdsBySection(UUID sectionId);
    Enrollment cancelEnrollment(UUID id);
    Enrollment addSection(UUID enrollmentId, UUID sectionId);
    Enrollment dropSection(UUID enrollmentId, UUID sectionId);
}
