package com.panadi.ums.attendanceservice.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface EnrollmentRosterLookupPort {
    Set<UUID> getActiveStudentIdsBySection(UUID sectionId);
}
