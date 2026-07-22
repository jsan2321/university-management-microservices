package com.panadi.ums.assignmentservice.application.port.in;

import com.panadi.ums.assignmentservice.application.PageResult;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.CreateAssignment;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.GradeSubmission;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.SubmitAssignment;
import com.panadi.ums.assignmentservice.domain.model.Assignment;
import com.panadi.ums.assignmentservice.domain.model.AssignmentStatus;
import com.panadi.ums.assignmentservice.domain.model.Submission;

import java.util.UUID;

public interface AssignmentUseCase {
    Assignment createAssignment(CreateAssignment command);
    Assignment getAssignment(UUID id);
    PageResult<Assignment> listAssignments(UUID sectionId, AssignmentStatus status, int page, int size);
    Assignment publishAssignment(UUID id, UUID teacherId);
    Assignment closeAssignment(UUID id, UUID teacherId);
    Submission submit(UUID assignmentId, SubmitAssignment command);
    Submission getSubmission(UUID id);
    PageResult<Submission> listSubmissions(UUID assignmentId, UUID studentId, int page, int size);
    Submission gradeSubmission(UUID submissionId, GradeSubmission command);
    Submission releaseGrade(UUID submissionId, UUID teacherId);
}
