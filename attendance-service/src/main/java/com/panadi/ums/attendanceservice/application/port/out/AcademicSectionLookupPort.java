package com.panadi.ums.attendanceservice.application.port.out;

import java.util.UUID;

public interface AcademicSectionLookupPort {
    SectionSnapshot getSection(UUID sectionId);

    record SectionSnapshot(UUID id, UUID teacherId, String status) {
        public boolean isActive() {
            return "ACTIVE".equals(status);
        }
    }
}
