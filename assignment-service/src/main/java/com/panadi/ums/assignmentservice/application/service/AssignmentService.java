package com.panadi.ums.assignmentservice.application.service;

import com.panadi.ums.assignmentservice.application.ApplicationException;
import com.panadi.ums.assignmentservice.application.PageResult;
import com.panadi.ums.assignmentservice.application.ResourceNotFoundException;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.CreateAssignment;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.GradeSubmission;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.SubmitAssignment;
import com.panadi.ums.assignmentservice.application.port.in.AssignmentUseCase;
import com.panadi.ums.assignmentservice.application.port.out.AcademicSectionLookupPort;
import com.panadi.ums.assignmentservice.application.port.out.AssignmentRepositoryPort;
import com.panadi.ums.assignmentservice.application.port.out.EnrollmentRosterLookupPort;
import com.panadi.ums.assignmentservice.domain.model.Assignment;
import com.panadi.ums.assignmentservice.domain.model.AssignmentStatus;
import com.panadi.ums.assignmentservice.domain.model.Submission;

import java.util.Set;
import java.util.UUID;

public class AssignmentService implements AssignmentUseCase {
    private final AssignmentRepositoryPort repository;
    private final AcademicSectionLookupPort academicSections;
    private final EnrollmentRosterLookupPort enrollmentRoster;

    public AssignmentService(AssignmentRepositoryPort repository, AcademicSectionLookupPort academicSections, EnrollmentRosterLookupPort enrollmentRoster) {
        this.repository = repository;
        this.academicSections = academicSections;
        this.enrollmentRoster = enrollmentRoster;
    }

    @Override
    public Assignment createAssignment(CreateAssignment command) {
        AcademicSectionLookupPort.SectionSnapshot section = academicSections.getSection(command.sectionId());
        if (!section.isActive()) throw new ApplicationException("Section is not active");
        if (!section.teacherId().equals(command.teacherId())) {
            throw new ApplicationException("Only the teacher assigned to the section can create assignments");
        }
        return repository.saveAssignment(Assignment.create(command.sectionId(), command.teacherId(), command.title(), command.description(), command.dueAt(), command.maxPoints()));
    }

    @Override
    public Assignment getAssignment(UUID id) {
        return repository.findAssignmentById(id).orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    @Override
    public PageResult<Assignment> listAssignments(UUID sectionId, AssignmentStatus status, int page, int size) {
        return repository.findAssignments(sectionId, status, page, size);
    }

    @Override
    public Assignment publishAssignment(UUID id, UUID teacherId) {
        Assignment assignment = getAssignment(id);
        assignment.verifyTeacher(teacherId);
        return repository.saveAssignment(assignment.publish());
    }

    @Override
    public Assignment closeAssignment(UUID id, UUID teacherId) {
        Assignment assignment = getAssignment(id);
        assignment.verifyTeacher(teacherId);
        return repository.saveAssignment(assignment.close());
    }

    @Override
    public Submission submit(UUID assignmentId, SubmitAssignment command) {
        Assignment assignment = getAssignment(assignmentId);
        if (assignment.status() != AssignmentStatus.PUBLISHED) {
            throw new ApplicationException("Assignment is not open for submissions");
        }
        Set<UUID> activeStudents = enrollmentRoster.getActiveStudentIdsBySection(assignment.sectionId());
        if (!activeStudents.contains(command.studentId())) {
            throw new ApplicationException("Student is not actively enrolled in this section");
        }
        if (repository.existsSubmission(assignmentId, command.studentId())) {
            throw new ApplicationException("Student has already submitted this assignment");
        }
        return repository.saveSubmission(Submission.create(assignmentId, command.studentId(), command.content(), assignment.dueAt()));
    }

    @Override
    public Submission getSubmission(UUID id) {
        return repository.findSubmissionById(id).orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    }

    @Override
    public PageResult<Submission> listSubmissions(UUID assignmentId, UUID studentId, int page, int size) {
        getAssignment(assignmentId);
        return repository.findSubmissions(assignmentId, studentId, page, size);
    }

    @Override
    public Submission gradeSubmission(UUID submissionId, GradeSubmission command) {
        Submission submission = getSubmission(submissionId);
        Assignment assignment = getAssignment(submission.assignmentId());
        assignment.verifyTeacher(command.teacherId());
        return repository.saveSubmission(submission.grade(command.score(), command.feedback(), assignment.maxPoints()));
    }

    @Override
    public Submission releaseGrade(UUID submissionId, UUID teacherId) {
        Submission submission = getSubmission(submissionId);
        Assignment assignment = getAssignment(submission.assignmentId());
        assignment.verifyTeacher(teacherId);
        return repository.saveSubmission(submission.releaseGrade());
    }
}
