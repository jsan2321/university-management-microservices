package com.panadi.ums.attendanceservice.domain.model;

import java.util.UUID;

public record AttendancePercentage(
        UUID studentId,
        UUID sectionId,
        long presentCount,
        long totalSessions,
        double percentage,
        boolean eligibleForFinalEvaluation
) {
    private static final double THRESHOLD = 70.0;

    public static AttendancePercentage calculate(UUID studentId, UUID sectionId, long presentCount, long totalSessions) {
        double value = totalSessions == 0 ? 0.0 : (presentCount * 100.0) / totalSessions;
        return new AttendancePercentage(studentId, sectionId, presentCount, totalSessions, value, value >= THRESHOLD);
    }
}
