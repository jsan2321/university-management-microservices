package com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest;

import com.panadi.ums.auditcommon.AuditOutbox;
import com.panadi.ums.attendanceservice.application.PageResult;
import com.panadi.ums.attendanceservice.application.command.CreateAttendanceSessionCommand;
import com.panadi.ums.attendanceservice.application.command.RecordAttendanceCommand;
import com.panadi.ums.attendanceservice.application.port.in.AttendanceUseCase;
import com.panadi.ums.attendanceservice.application.port.out.AcademicSectionLookupPort;
import com.panadi.ums.attendanceservice.application.port.out.EnrollmentRosterLookupPort;
import com.panadi.ums.attendanceservice.domain.model.Attendance;
import com.panadi.ums.attendanceservice.domain.model.AttendancePercentage;
import com.panadi.ums.attendanceservice.domain.model.AttendanceSession;
import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;
import com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto.AttendanceDtos.AttendancePercentageResponse;
import com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto.AttendanceDtos.AttendanceResponse;
import com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto.AttendanceDtos.AttendanceSessionResponse;
import com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto.AttendanceDtos.CreateAttendanceSessionRequest;
import com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto.AttendanceDtos.PageResponse;
import com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto.AttendanceDtos.RecordAttendanceRequest;
import com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto.AttendanceDtos.SectionRosterResponse;
import com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest.dto.AttendanceDtos.RosterStudentResponse;
import com.panadi.ums.security.CurrentActor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/attendance")
class AttendanceController {
    private final AttendanceUseCase useCase;
    private final AcademicSectionLookupPort sections;
    private final EnrollmentRosterLookupPort enrollmentRoster;
    private final AttendanceActorTeacherClient teachers;
    private final AttendanceActorStudentClient students;
    private final AuditOutbox audit;

    AttendanceController(AttendanceUseCase useCase, AcademicSectionLookupPort sections, EnrollmentRosterLookupPort enrollmentRoster,
                         AttendanceActorTeacherClient teachers, AttendanceActorStudentClient students, AuditOutbox audit) {
        this.useCase = useCase;
        this.sections = sections;
        this.enrollmentRoster = enrollmentRoster;
        this.teachers = teachers;
        this.students = students;
        this.audit = audit;
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    AttendanceSessionResponse createSession(@Valid @RequestBody CreateAttendanceSessionRequest request) {
        requireTeacherSection(request.sectionId());
        return toResponse(useCase.createSession(new CreateAttendanceSessionCommand(request.sectionId(), request.sessionNumber(), request.date(), request.topic())));
    }

    @GetMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    AttendanceSessionResponse getSession(@PathVariable UUID id) {
        AttendanceSession value = useCase.getSession(id);
        requireTeacherSection(value.sectionId());
        return toResponse(value);
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    PageResponse<AttendanceSessionResponse> listSessions(@RequestParam(required = false) UUID sectionId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        requireTeacherSection(sectionId);
        return toPage(useCase.listSessions(sectionId, page, size), this::toResponse);
    }

    @PostMapping("/sessions/{sessionId}/records")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Transactional
    List<AttendanceResponse> recordAttendance(@PathVariable UUID sessionId, @Valid @RequestBody RecordAttendanceRequest request) {
        requireTeacherSection(useCase.getSession(sessionId).sectionId());
        List<RecordAttendanceCommand.Record> records = request.records().stream()
                .map(record -> new RecordAttendanceCommand.Record(record.studentId(), record.status()))
                .toList();
        List<Attendance> saved = useCase.recordAttendance(sessionId, new RecordAttendanceCommand(records));
        AttendanceSession session = useCase.getSession(sessionId);
        audit.record("AttendanceRecorded", "attendance-service", "AttendanceSession", sessionId, null,
                Map.of("sectionId", session.sectionId(), "recordCount", saved.size()));
        return saved.stream().map(this::toResponse).toList();
    }

    @GetMapping("/sessions/{sessionId}/records")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    PageResponse<AttendanceResponse> listRecords(@PathVariable UUID sessionId, @RequestParam(required = false) AttendanceStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        requireTeacherSection(useCase.getSession(sessionId).sectionId());
        return toPage(useCase.listRecords(sessionId, status, page, size), this::toResponse);
    }

    @GetMapping("/sections/{sectionId}/students")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    SectionRosterResponse sectionRoster(@PathVariable UUID sectionId) {
        requireTeacherSection(sectionId);
        List<RosterStudentResponse> roster = enrollmentRoster.getActiveStudentIdsBySection(sectionId).stream()
                .sorted()
                .map(students::byId)
                .map(student -> new RosterStudentResponse(student.id(), student.studentCode(), student.firstName(), student.lastName()))
                .toList();
        return new SectionRosterResponse(sectionId, roster);
    }

    @GetMapping("/students/{studentId}/sections/{sectionId}/percentage")
    @PreAuthorize("hasRole('ADMIN')")
    AttendancePercentageResponse calculatePercentage(@PathVariable UUID studentId, @PathVariable UUID sectionId) {
        return toResponse(useCase.calculatePercentage(studentId, sectionId));
    }

    @GetMapping("/me/sections/{sectionId}/percentage")
    @PreAuthorize("hasRole('STUDENT')")
    AttendancePercentageResponse myPercentage(@PathVariable UUID sectionId) {
        UUID studentId = students.byUser(CurrentActor.required().userId()).id();
        return toResponse(useCase.calculatePercentage(studentId, sectionId));
    }

    private void requireTeacherSection(UUID sectionId) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("ADMIN")) return;
        if (!actor.hasRole("TEACHER") || sectionId == null) {
            throw new AccessDeniedException("A section assigned to the authenticated teacher is required");
        }
        UUID teacherId = teachers.byUser(actor.userId()).id();
        if (!teacherId.equals(sections.getSection(sectionId).teacherId())) {
            throw new AccessDeniedException("Section is not assigned to the authenticated teacher");
        }
    }

    private AttendanceSessionResponse toResponse(AttendanceSession session) {
        return new AttendanceSessionResponse(session.id(), session.sectionId(), session.sessionNumber(), session.date(), session.topic(), session.createdAt(), session.updatedAt());
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        return new AttendanceResponse(attendance.id(), attendance.attendanceSessionId(), attendance.studentId(), attendance.status(), attendance.recordedAt(), attendance.updatedAt());
    }

    private AttendancePercentageResponse toResponse(AttendancePercentage percentage) {
        return new AttendancePercentageResponse(percentage.studentId(), percentage.sectionId(), percentage.presentCount(), percentage.totalSessions(), percentage.percentage(), percentage.eligibleForFinalEvaluation());
    }

    private <T, R> PageResponse<R> toPage(PageResult<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.content().stream().map(mapper).toList(), page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
