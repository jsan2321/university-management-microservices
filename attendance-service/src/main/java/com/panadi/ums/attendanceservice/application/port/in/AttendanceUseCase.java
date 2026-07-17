package com.panadi.ums.attendanceservice.application.port.in;

import com.panadi.ums.attendanceservice.application.PageResult;
import com.panadi.ums.attendanceservice.application.command.CreateAttendanceSessionCommand;
import com.panadi.ums.attendanceservice.application.command.RecordAttendanceCommand;
import com.panadi.ums.attendanceservice.domain.model.Attendance;
import com.panadi.ums.attendanceservice.domain.model.AttendancePercentage;
import com.panadi.ums.attendanceservice.domain.model.AttendanceSession;
import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;

import java.util.List;
import java.util.UUID;

public interface AttendanceUseCase {
    AttendanceSession createSession(CreateAttendanceSessionCommand command);
    AttendanceSession getSession(UUID id);
    PageResult<AttendanceSession> listSessions(UUID sectionId, int page, int size);
    List<Attendance> recordAttendance(UUID sessionId, RecordAttendanceCommand command);
    PageResult<Attendance> listRecords(UUID sessionId, AttendanceStatus status, int page, int size);
    AttendancePercentage calculatePercentage(UUID studentId, UUID sectionId);
}
