package com.panadi.ums.assignmentservice.application.service;

import com.panadi.ums.assignmentservice.application.ApplicationException;
import com.panadi.ums.assignmentservice.application.PageResult;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.CreateAssignment;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.GradeSubmission;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.SubmitAssignment;
import com.panadi.ums.assignmentservice.application.port.out.AcademicSectionLookupPort;
import com.panadi.ums.assignmentservice.application.port.out.AssignmentRepositoryPort;
import com.panadi.ums.assignmentservice.application.port.out.EnrollmentRosterLookupPort;
import com.panadi.ums.assignmentservice.domain.model.Assignment;
import com.panadi.ums.assignmentservice.domain.model.Submission;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssignmentServiceTests {
    private final UUID sectionId = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();
    private final UUID studentId = UUID.randomUUID();

    @Test
    void onlySectionTeacherCanCreateAssignment() {
        assertThatThrownBy(() -> service(repository(), roster(studentId)).createAssignment(
                new CreateAssignment(sectionId, UUID.randomUUID(), "Project", null, LocalDateTime.now().plusDays(7), BigDecimal.valueOf(100))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("assigned");
    }

    @Test
    void enrolledStudentCanSubmitPublishedAssignment() {
        AssignmentRepositoryPort repository = repository();
        Assignment assignment = assignment().publish();
        when(repository.findAssignmentById(assignment.id())).thenReturn(Optional.of(assignment));

        Submission submission = service(repository, roster(studentId)).submit(assignment.id(), new SubmitAssignment(studentId, "My solution"));

        assertThat(submission.studentId()).isEqualTo(studentId);
        assertThat(submission.score()).isNull();
    }

    @Test
    void nonEnrolledStudentCannotSubmit() {
        AssignmentRepositoryPort repository = repository();
        Assignment assignment = assignment().publish();
        when(repository.findAssignmentById(assignment.id())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> service(repository, roster(UUID.randomUUID())).submit(assignment.id(), new SubmitAssignment(studentId, "My solution")))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("not actively enrolled");
    }

    @Test
    void assignedTeacherCanGradeAndRelease() {
        AssignmentRepositoryPort repository = repository();
        Assignment assignment = assignment().publish();
        Submission submission = new Submission(UUID.randomUUID(), assignment.id(), studentId, "solution", com.panadi.ums.assignmentservice.domain.model.SubmissionStatus.ON_TIME, null, null, LocalDateTime.now(), null, null, LocalDateTime.now());
        when(repository.findSubmissionById(submission.id())).thenReturn(Optional.of(submission));
        when(repository.findAssignmentById(assignment.id())).thenReturn(Optional.of(assignment));

        Submission graded = service(repository, roster(studentId)).gradeSubmission(submission.id(), new GradeSubmission(teacherId, BigDecimal.valueOf(90), "Good work"));
        when(repository.findSubmissionById(submission.id())).thenReturn(Optional.of(graded));
        Submission released = service(repository, roster(studentId)).releaseGrade(submission.id(), teacherId);

        assertThat(released.score()).isEqualByComparingTo("90");
        assertThat(released.gradeReleased()).isTrue();
    }

    private AssignmentService service(AssignmentRepositoryPort repository, EnrollmentRosterLookupPort roster) {
        AcademicSectionLookupPort sections = mock(AcademicSectionLookupPort.class);
        when(sections.getSection(sectionId)).thenReturn(new AcademicSectionLookupPort.SectionSnapshot(sectionId, teacherId, "ACTIVE"));
        return new AssignmentService(repository, sections, roster);
    }

    private AssignmentRepositoryPort repository() {
        AssignmentRepositoryPort repository = mock(AssignmentRepositoryPort.class);
        when(repository.saveAssignment(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveSubmission(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findAssignments(any(), any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));
        return repository;
    }

    private EnrollmentRosterLookupPort roster(UUID enrolledStudentId) {
        EnrollmentRosterLookupPort roster = mock(EnrollmentRosterLookupPort.class);
        when(roster.getActiveStudentIdsBySection(sectionId)).thenReturn(Set.of(enrolledStudentId));
        return roster;
    }

    private Assignment assignment() {
        LocalDateTime now = LocalDateTime.now();
        return new Assignment(UUID.randomUUID(), sectionId, teacherId, "Project", "Build it", now.plusDays(7), BigDecimal.valueOf(100), com.panadi.ums.assignmentservice.domain.model.AssignmentStatus.DRAFT, now, now, null, null);
    }
}
