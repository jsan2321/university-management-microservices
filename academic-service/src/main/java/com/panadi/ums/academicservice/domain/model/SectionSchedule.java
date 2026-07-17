package com.panadi.ums.academicservice.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record SectionSchedule(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
    public SectionSchedule {
        if (dayOfWeek == null) {
            throw new DomainValidationException("Schedule day is required");
        }
        if (startTime == null || endTime == null) {
            throw new DomainValidationException("Schedule times are required");
        }
        if (!startTime.isBefore(endTime)) {
            throw new DomainValidationException("Schedule start time must be before end time");
        }
    }
}
