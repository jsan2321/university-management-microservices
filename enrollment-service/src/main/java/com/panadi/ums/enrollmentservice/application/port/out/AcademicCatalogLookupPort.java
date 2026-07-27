package com.panadi.ums.enrollmentservice.application.port.out;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AcademicCatalogLookupPort {
    SemesterSnapshot getSemester(UUID semesterId);
    SectionSnapshot getSection(UUID sectionId);
    SubjectSnapshot getSubject(UUID subjectId);

    record SemesterSnapshot(UUID id, String name, String status) {
        public boolean isActive() {
            return "ACTIVE".equals(status);
        }
    }

    record SectionSnapshot(UUID id, UUID subjectId, UUID teacherId, UUID semesterId, String sectionCode, int capacity, String status, List<ScheduleSnapshot> schedules) {
        public boolean isActive() {
            return "ACTIVE".equals(status);
        }
    }

    record SubjectSnapshot(UUID id, UUID programId, String code, String name, int credits, Set<UUID> prerequisiteSubjectIds, String status) {
        public boolean isActive() {
            return "ACTIVE".equals(status);
        }
    }

    record ScheduleSnapshot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        public boolean overlaps(ScheduleSnapshot other) {
            return dayOfWeek == other.dayOfWeek && startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
        }
    }
}
