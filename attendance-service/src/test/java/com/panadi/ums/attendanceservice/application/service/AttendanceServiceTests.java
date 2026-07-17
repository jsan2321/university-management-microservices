package com.panadi.ums.attendanceservice.application.service;

import com.panadi.ums.attendanceservice.application.ApplicationException;
import com.panadi.ums.attendanceservice.application.PageResult;
import com.panadi.ums.attendanceservice.application.command.CreateAttendanceSessionCommand;
import com.panadi.ums.attendanceservice.application.command.RecordAttendanceCommand;
import com.panadi.ums.attendanceservice.application.port.out.AcademicSectionLookupPort;
import com.panadi.ums.attendanceservice.application.port.out.AttendanceRepositoryPort;
import com.panadi.ums.attendanceservice.application.port.out.EnrollmentRosterLookupPort;
import com.panadi.ums.attendanceservice.domain.model.Attendance;
import com.panadi.ums.attendanceservice.domain.model.AttendanceSession;
import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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

class AttendanceServiceTests {
    private final UUID sectionId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID studentId = UUID.randomUUID();

    @Test
    void rejectsInactiveSectionWhenCreatingSession() {
        assertThatThrownBy(() -> service(repository(), section("INACTIVE"), roster(studentId))
                .createSession(new CreateAttendanceSessionCommand(sectionId, 1, LocalDate.now(), "Intro")))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Section");
    }

    @Test
    void rejectsDuplicateSessionNumber() {
        AttendanceRepositoryPort repository = repository();
        when(repository.existsSessionBySectionIdAndSessionNumber(sectionId, 1)).thenReturn(true);

        assertThatThrownBy(() -> service(repository, section("ACTIVE"), roster(studentId))
                .createSession(new CreateAttendanceSessionCommand(sectionId, 1, LocalDate.now(), "Intro")))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void recordsAttendanceForEnrolledStudent() {
        AttendanceRepositoryPort repository = repository();
        AttendanceSession session = session();
        when(repository.findSessionById(sessionId)).thenReturn(Optional.of(session));

        List<Attendance> records = service(repository, section("ACTIVE"), roster(studentId))
                .recordAttendance(sessionId, new RecordAttendanceCommand(List.of(new RecordAttendanceCommand.Record(studentId, AttendanceStatus.PRESENT))));

        assertThat(records).hasSize(1);
        assertThat(records.getFirst().status()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void upsertsExistingAttendanceRecord() {
        AttendanceRepositoryPort repository = repository();
        Attendance existing = new Attendance(UUID.randomUUID(), sessionId, studentId, AttendanceStatus.ABSENT, LocalDateTime.now(), LocalDateTime.now());
        when(repository.findSessionById(sessionId)).thenReturn(Optional.of(session()));
        when(repository.findAttendance(sessionId, studentId)).thenReturn(Optional.of(existing));

        List<Attendance> records = service(repository, section("ACTIVE"), roster(studentId))
                .recordAttendance(sessionId, new RecordAttendanceCommand(List.of(new RecordAttendanceCommand.Record(studentId, AttendanceStatus.PRESENT))));

        assertThat(records.getFirst().id()).isEqualTo(existing.id());
        assertThat(records.getFirst().status()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void rejectsNonEnrolledStudent() {
        AttendanceRepositoryPort repository = repository();
        when(repository.findSessionById(sessionId)).thenReturn(Optional.of(session()));

        assertThatThrownBy(() -> service(repository, section("ACTIVE"), roster(UUID.randomUUID()))
                .recordAttendance(sessionId, new RecordAttendanceCommand(List.of(new RecordAttendanceCommand.Record(studentId, AttendanceStatus.PRESENT)))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("not actively enrolled");
    }

    @Test
    void rejectsDuplicateStudentRecords() {
        AttendanceRepositoryPort repository = repository();
        when(repository.findSessionById(sessionId)).thenReturn(Optional.of(session()));

        assertThatThrownBy(() -> service(repository, section("ACTIVE"), roster(studentId))
                .recordAttendance(sessionId, new RecordAttendanceCommand(List.of(
                        new RecordAttendanceCommand.Record(studentId, AttendanceStatus.PRESENT),
                        new RecordAttendanceCommand.Record(studentId, AttendanceStatus.ABSENT)
                ))))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void calculatesAttendancePercentage() {
        AttendanceRepositoryPort repository = repository();
        when(repository.countSessionsBySectionId(sectionId)).thenReturn(10L);
        when(repository.countPresentByStudentIdAndSectionId(studentId, sectionId)).thenReturn(8L);

        var percentage = service(repository, section("ACTIVE"), roster(studentId)).calculatePercentage(studentId, sectionId);

        assertThat(percentage.percentage()).isEqualTo(80.0);
        assertThat(percentage.eligibleForFinalEvaluation()).isTrue();
    }

    private AttendanceService service(AttendanceRepositoryPort repository, AcademicSectionLookupPort sections, EnrollmentRosterLookupPort roster) {
        return new AttendanceService(repository, sections, roster);
    }

    private AttendanceRepositoryPort repository() {
        AttendanceRepositoryPort repository = mock(AttendanceRepositoryPort.class);
        when(repository.saveSession(any(AttendanceSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveAttendance(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findSessions(any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));
        return repository;
    }

    private AcademicSectionLookupPort section(String status) {
        AcademicSectionLookupPort sections = mock(AcademicSectionLookupPort.class);
        when(sections.getSection(sectionId)).thenReturn(new AcademicSectionLookupPort.SectionSnapshot(sectionId, UUID.randomUUID(), status));
        return sections;
    }

    private EnrollmentRosterLookupPort roster(UUID studentId) {
        EnrollmentRosterLookupPort roster = mock(EnrollmentRosterLookupPort.class);
        when(roster.getActiveStudentIdsBySection(sectionId)).thenReturn(Set.of(studentId));
        return roster;
    }

    private AttendanceSession session() {
        return new AttendanceSession(sessionId, sectionId, 1, LocalDate.now(), "Intro", LocalDateTime.now(), LocalDateTime.now());
    }
}
