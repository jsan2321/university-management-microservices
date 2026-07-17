package com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest;

import com.panadi.ums.attendanceservice.application.PageResult;
import com.panadi.ums.attendanceservice.application.command.CreateAttendanceSessionCommand;
import com.panadi.ums.attendanceservice.application.command.RecordAttendanceCommand;
import com.panadi.ums.attendanceservice.application.port.in.AttendanceUseCase;
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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/attendance")
class AttendanceController {
    private final AttendanceUseCase useCase;

    AttendanceController(AttendanceUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    AttendanceSessionResponse createSession(@Valid @RequestBody CreateAttendanceSessionRequest request) {
        return toResponse(useCase.createSession(new CreateAttendanceSessionCommand(request.sectionId(), request.sessionNumber(), request.date(), request.topic())));
    }

    @GetMapping("/sessions/{id}")
    AttendanceSessionResponse getSession(@PathVariable UUID id) {
        return toResponse(useCase.getSession(id));
    }

    @GetMapping("/sessions")
    PageResponse<AttendanceSessionResponse> listSessions(@RequestParam(required = false) UUID sectionId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return toPage(useCase.listSessions(sectionId, page, size), this::toResponse);
    }

    @PostMapping("/sessions/{sessionId}/records")
    List<AttendanceResponse> recordAttendance(@PathVariable UUID sessionId, @Valid @RequestBody RecordAttendanceRequest request) {
        List<RecordAttendanceCommand.Record> records = request.records().stream()
                .map(record -> new RecordAttendanceCommand.Record(record.studentId(), record.status()))
                .toList();
        return useCase.recordAttendance(sessionId, new RecordAttendanceCommand(records)).stream().map(this::toResponse).toList();
    }

    @GetMapping("/sessions/{sessionId}/records")
    PageResponse<AttendanceResponse> listRecords(@PathVariable UUID sessionId, @RequestParam(required = false) AttendanceStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return toPage(useCase.listRecords(sessionId, status, page, size), this::toResponse);
    }

    @GetMapping("/students/{studentId}/sections/{sectionId}/percentage")
    AttendancePercentageResponse calculatePercentage(@PathVariable UUID studentId, @PathVariable UUID sectionId) {
        return toResponse(useCase.calculatePercentage(studentId, sectionId));
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
