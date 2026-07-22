package com.panadi.ums.assignmentservice.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface EnrollmentRosterLookupPort {
    Set<UUID> getActiveStudentIdsBySection(UUID sectionId);
}
