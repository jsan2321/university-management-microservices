package com.panadi.ums.enrollmentservice.application.service;

import com.panadi.ums.enrollmentservice.application.ApplicationException;
import com.panadi.ums.enrollmentservice.application.DependencyUnavailableException;
import com.panadi.ums.enrollmentservice.application.command.CreateEnrollmentCommand;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort.ScheduleSnapshot;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort.SectionSnapshot;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort.SemesterSnapshot;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort.SubjectSnapshot;
import com.panadi.ums.enrollmentservice.application.port.out.EnrollmentRepositoryPort;
import com.panadi.ums.enrollmentservice.application.port.out.StudentLookupPort;
import com.panadi.ums.enrollmentservice.domain.model.Enrollment;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentDetail;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnrollmentServiceTests {
    private final UUID studentId = UUID.randomUUID();
    private final UUID programId = UUID.randomUUID();
    private final UUID semesterId = UUID.randomUUID();
    private final UUID sectionId = UUID.randomUUID();
    private final UUID subjectId = UUID.randomUUID();

    @Test
    void createsEnrollmentWhenRulesPass() {
        EnrollmentRepositoryPort repository = repository();

        Enrollment created = service(repository, student("ACTIVE"), semester("ACTIVE"), section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), subject(subjectId, programId, "ACTIVE", 4))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId)));

        assertThat(created.studentId()).isEqualTo(studentId);
        assertThat(created.totalCredits()).isEqualTo(4);
    }

    @Test
    void rejectsInactiveStudent() {
        assertThatThrownBy(() -> service(repository(), student("SUSPENDED"), semester("ACTIVE"), section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), subject(subjectId, programId, "ACTIVE", 4))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Student");
    }

    @Test
    void rejectsInactiveSemester() {
        assertThatThrownBy(() -> service(repository(), student("ACTIVE"), semester("CLOSED"), section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), subject(subjectId, programId, "ACTIVE", 4))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Semester");
    }

    @Test
    void rejectsInactiveSection() {
        assertThatThrownBy(() -> service(repository(), student("ACTIVE"), semester("ACTIVE"), section(sectionId, subjectId, "INACTIVE", 30, schedule(8, 10)), subject(subjectId, programId, "ACTIVE", 4))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Section");
    }

    @Test
    void rejectsInactiveSubject() {
        assertThatThrownBy(() -> service(repository(), student("ACTIVE"), semester("ACTIVE"), section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), subject(subjectId, programId, "INACTIVE", 4))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Subject");
    }

    @Test
    void rejectsWrongProgram() {
        assertThatThrownBy(() -> service(repository(), student("ACTIVE"), semester("ACTIVE"), section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), subject(subjectId, UUID.randomUUID(), "ACTIVE", 4))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("program");
    }

    @Test
    void rejectsDuplicateActiveEnrollment() {
        EnrollmentRepositoryPort repository = repository();
        when(repository.existsActiveEnrollment(studentId, semesterId)).thenReturn(true);

        assertThatThrownBy(() -> service(repository, student("ACTIVE"), semester("ACTIVE"), section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), subject(subjectId, programId, "ACTIVE", 4))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("active enrollment");
    }

    @Test
    void rejectsFullCapacity() {
        EnrollmentRepositoryPort repository = repository();
        when(repository.countActiveEnrollmentDetailsBySectionId(sectionId)).thenReturn(30L);

        assertThatThrownBy(() -> service(repository, student("ACTIVE"), semester("ACTIVE"), section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), subject(subjectId, programId, "ACTIVE", 4))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    void rejectsMaxCreditsExceeded() {
        UUID secondSectionId = UUID.randomUUID();
        UUID secondSubjectId = UUID.randomUUID();
        EnrollmentRepositoryPort repository = repository();
        AcademicCatalogLookupPort academic = academic(semester("ACTIVE"), List.of(section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), section(secondSectionId, secondSubjectId, "ACTIVE", 30, schedule(10, 12))), List.of(subject(subjectId, programId, "ACTIVE", 12), subject(secondSubjectId, programId, "ACTIVE", 11)));

        assertThatThrownBy(() -> new EnrollmentService(repository, student("ACTIVE"), academic)
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId, secondSectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("credits");
    }

    @Test
    void rejectsScheduleConflict() {
        UUID secondSectionId = UUID.randomUUID();
        UUID secondSubjectId = UUID.randomUUID();
        AcademicCatalogLookupPort academic = academic(semester("ACTIVE"), List.of(section(sectionId, subjectId, "ACTIVE", 30, schedule(8, 10)), section(secondSectionId, secondSubjectId, "ACTIVE", 30, schedule(9, 11))), List.of(subject(subjectId, programId, "ACTIVE", 4), subject(secondSubjectId, programId, "ACTIVE", 4)));

        assertThatThrownBy(() -> new EnrollmentService(repository(), student("ACTIVE"), academic)
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId, secondSectionId))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("conflict");
    }

    @Test
    void failsWhenDependencyUnavailable() {
        StudentLookupPort students = mock(StudentLookupPort.class);
        doThrow(new DependencyUnavailableException("Student Service is unavailable")).when(students).getStudent(studentId);

        assertThatThrownBy(() -> new EnrollmentService(repository(), students, mock(AcademicCatalogLookupPort.class))
                .createEnrollment(new CreateEnrollmentCommand(studentId, semesterId, List.of(sectionId))))
                .isInstanceOf(DependencyUnavailableException.class);
    }

    @Test
    void cancelsEnrollment() {
        EnrollmentRepositoryPort repository = repository();
        UUID enrollmentId = UUID.randomUUID();
        Enrollment enrollment = new Enrollment(enrollmentId, studentId, semesterId, com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus.ACTIVE, List.of(EnrollmentDetail.create(sectionId, subjectId, 4)), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), null);
        when(repository.findEnrollmentById(enrollment.id())).thenReturn(Optional.of(enrollment));

        Enrollment cancelled = new EnrollmentService(repository, mock(StudentLookupPort.class), mock(AcademicCatalogLookupPort.class)).cancelEnrollment(enrollment.id());

        assertThat(cancelled.cancelledAt()).isNotNull();
    }

    @Test
    void listsActiveStudentsBySection() {
        EnrollmentRepositoryPort repository = repository();
        when(repository.findActiveStudentIdsBySectionId(sectionId)).thenReturn(List.of(studentId));

        List<UUID> studentIds = new EnrollmentService(repository, mock(StudentLookupPort.class), mock(AcademicCatalogLookupPort.class))
                .listActiveStudentIdsBySection(sectionId);

        assertThat(studentIds).containsExactly(studentId);
    }

    private EnrollmentRepositoryPort repository() {
        EnrollmentRepositoryPort repository = mock(EnrollmentRepositoryPort.class);
        when(repository.saveEnrollment(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return repository;
    }

    private EnrollmentService service(EnrollmentRepositoryPort repository, StudentLookupPort students, SemesterSnapshot semester, SectionSnapshot section, SubjectSnapshot subject) {
        return new EnrollmentService(repository, students, academic(semester, section, subject));
    }

    private StudentLookupPort student(String status) {
        StudentLookupPort students = mock(StudentLookupPort.class);
        when(students.getStudent(studentId)).thenReturn(new StudentLookupPort.StudentSnapshot(studentId, programId, status));
        return students;
    }

    private AcademicCatalogLookupPort academic(SemesterSnapshot semester, SectionSnapshot section, SubjectSnapshot subject) {
        return academic(semester, List.of(section), List.of(subject));
    }

    private AcademicCatalogLookupPort academic(SemesterSnapshot semester, List<SectionSnapshot> sections, List<SubjectSnapshot> subjects) {
        AcademicCatalogLookupPort academic = mock(AcademicCatalogLookupPort.class);
        when(academic.getSemester(semesterId)).thenReturn(semester);
        sections.forEach(section -> when(academic.getSection(section.id())).thenReturn(section));
        subjects.forEach(subject -> when(academic.getSubject(subject.id())).thenReturn(subject));
        return academic;
    }

    private SemesterSnapshot semester(String status) {
        return new SemesterSnapshot(semesterId, "Semester 2026", status, true);
    }

    private SectionSnapshot section(UUID id, UUID subjectId, String status, int capacity, ScheduleSnapshot schedule) {
        return new SectionSnapshot(id, subjectId, UUID.randomUUID(), semesterId, "SEC-A", capacity, status, List.of(schedule));
    }

    private SubjectSnapshot subject(UUID id, UUID programId, String status, int credits) {
        return new SubjectSnapshot(id, programId, "SUB-101", "Subject name", credits, Set.of(), status);
    }

    private ScheduleSnapshot schedule(int startHour, int endHour) {
        return new ScheduleSnapshot(DayOfWeek.MONDAY, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0));
    }
}
