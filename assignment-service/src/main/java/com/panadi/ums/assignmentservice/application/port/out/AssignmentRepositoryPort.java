package com.panadi.ums.assignmentservice.application.port.out;

import com.panadi.ums.assignmentservice.application.PageResult;
import com.panadi.ums.assignmentservice.domain.model.Assignment;
import com.panadi.ums.assignmentservice.domain.model.AssignmentStatus;
import com.panadi.ums.assignmentservice.domain.model.Submission;

import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepositoryPort {
    Assignment saveAssignment(Assignment assignment);
    Optional<Assignment> findAssignmentById(UUID id);
    PageResult<Assignment> findAssignments(UUID sectionId, AssignmentStatus status, int page, int size);
    Submission saveSubmission(Submission submission);
    Optional<Submission> findSubmissionById(UUID id);
    boolean existsSubmission(UUID assignmentId, UUID studentId);
    PageResult<Submission> findSubmissions(UUID assignmentId, UUID studentId, int page, int size);
}
