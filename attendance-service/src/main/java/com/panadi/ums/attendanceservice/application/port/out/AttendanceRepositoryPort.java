package com.panadi.ums.attendanceservice.application.port.out;

import com.panadi.ums.attendanceservice.application.PageResult;
import com.panadi.ums.attendanceservice.domain.model.Attendance;
import com.panadi.ums.attendanceservice.domain.model.AttendanceSession;
import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;

import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepositoryPort {
    AttendanceSession saveSession(AttendanceSession session);
    Optional<AttendanceSession> findSessionById(UUID id);
    PageResult<AttendanceSession> findSessions(UUID sectionId, int page, int size);
    boolean existsSessionBySectionIdAndSessionNumber(UUID sectionId, int sessionNumber);
    Attendance saveAttendance(Attendance attendance);
    Optional<Attendance> findAttendance(UUID sessionId, UUID studentId);
    PageResult<Attendance> findAttendances(UUID sessionId, AttendanceStatus status, int page, int size);
    long countSessionsBySectionId(UUID sectionId);
    long countPresentByStudentIdAndSectionId(UUID studentId, UUID sectionId);
    long countAttendancesByStudentIdAndSectionId(UUID studentId, UUID sectionId);
}
