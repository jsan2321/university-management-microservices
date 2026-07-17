package com.panadi.ums.attendanceservice.application.command;

import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;

import java.util.List;
import java.util.UUID;

public record RecordAttendanceCommand(List<Record> records) {
    public record Record(UUID studentId, AttendanceStatus status) {
    }
}
