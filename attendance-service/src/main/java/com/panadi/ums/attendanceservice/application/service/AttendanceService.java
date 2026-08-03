package com.panadi.ums.attendanceservice.application.service;

import com.panadi.ums.attendanceservice.application.ApplicationException;
import com.panadi.ums.attendanceservice.application.PageResult;
import com.panadi.ums.attendanceservice.application.ResourceNotFoundException;
import com.panadi.ums.attendanceservice.application.command.CreateAttendanceSessionCommand;
import com.panadi.ums.attendanceservice.application.command.RecordAttendanceCommand;
import com.panadi.ums.attendanceservice.application.port.in.AttendanceUseCase;
import com.panadi.ums.attendanceservice.application.port.out.AcademicSectionLookupPort;
import com.panadi.ums.attendanceservice.application.port.out.AttendanceRepositoryPort;
import com.panadi.ums.attendanceservice.application.port.out.EnrollmentRosterLookupPort;
import com.panadi.ums.attendanceservice.domain.model.Attendance;
import com.panadi.ums.attendanceservice.domain.model.AttendancePercentage;
import com.panadi.ums.attendanceservice.domain.model.AttendanceSession;
import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AttendanceService implements AttendanceUseCase {
    private final AttendanceRepositoryPort attendance;
    private final AcademicSectionLookupPort academicSections;
    private final EnrollmentRosterLookupPort enrollmentRoster;

    public AttendanceService(AttendanceRepositoryPort attendance, AcademicSectionLookupPort academicSections, EnrollmentRosterLookupPort enrollmentRoster) {
        this.attendance = attendance;
        this.academicSections = academicSections;
        this.enrollmentRoster = enrollmentRoster;
    }

    @Override
    public AttendanceSession createSession(CreateAttendanceSessionCommand command) {
        AcademicSectionLookupPort.SectionSnapshot section = academicSections.getSection(command.sectionId());
        if (!section.isActive()) {
            throw new ApplicationException("Section is not active");
        }
        if (attendance.existsSessionBySectionIdAndSessionNumber(command.sectionId(), command.sessionNumber())) {
            throw new ApplicationException("Attendance session already exists for this section and session number");
        }
        return attendance.saveSession(AttendanceSession.create(command.sectionId(), command.sessionNumber(), command.date(), command.topic()));
    }

    @Override
    public AttendanceSession getSession(UUID id) {
        return attendance.findSessionById(id).orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));
    }

    @Override
    public PageResult<AttendanceSession> listSessions(UUID sectionId, int page, int size) {
        return attendance.findSessions(sectionId, page, size);
    }

    @Override
    public List<Attendance> recordAttendance(UUID sessionId, RecordAttendanceCommand command) {
        AttendanceSession session = getSession(sessionId);
        validateRecordRequest(command);
        Set<UUID> activeStudentIds = enrollmentRoster.getActiveStudentIdsBySection(session.sectionId());
        return command.records().stream()
                .map(record -> {
                    if (!activeStudentIds.contains(record.studentId())) {
                        throw new ApplicationException("Student is not actively enrolled in this section");
                    }
                    Attendance value = attendance.findAttendance(sessionId, record.studentId())
                            .map(existing -> existing.updateStatus(record.status()))
                            .orElseGet(() -> Attendance.create(sessionId, record.studentId(), record.status()));
                    return attendance.saveAttendance(value);
                })
                .toList();
    }

    @Override
    public PageResult<Attendance> listRecords(UUID sessionId, AttendanceStatus status, int page, int size) {
        getSession(sessionId);
        return attendance.findAttendances(sessionId, status, page, size);
    }

    @Override
    public AttendancePercentage calculatePercentage(UUID studentId, UUID sectionId) {
        long totalSessions = attendance.countAttendancesByStudentIdAndSectionId(studentId, sectionId);
        long presentCount = attendance.countPresentByStudentIdAndSectionId(studentId, sectionId);
        return AttendancePercentage.calculate(studentId, sectionId, presentCount, totalSessions);
    }

    private void validateRecordRequest(RecordAttendanceCommand command) {
        if (command.records() == null || command.records().isEmpty()) {
            throw new ApplicationException("At least one attendance record is required");
        }
        Set<UUID> studentIds = new HashSet<>();
        command.records().forEach(record -> {
            if (record.studentId() == null) {
                throw new ApplicationException("Student id is required");
            }
            if (record.status() == null) {
                throw new ApplicationException("Attendance status is required");
            }
            if (!studentIds.add(record.studentId())) {
                throw new ApplicationException("Duplicate student attendance records are not allowed");
            }
        });
    }
}
