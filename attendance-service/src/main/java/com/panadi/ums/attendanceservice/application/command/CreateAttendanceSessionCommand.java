package com.panadi.ums.attendanceservice.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAttendanceSessionCommand(UUID sectionId, int sessionNumber, LocalDate date, String topic) {
}
