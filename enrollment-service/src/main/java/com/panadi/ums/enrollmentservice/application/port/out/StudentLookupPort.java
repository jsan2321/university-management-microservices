package com.panadi.ums.enrollmentservice.application.port.out;

import java.util.UUID;

public interface StudentLookupPort {
    StudentSnapshot getStudent(UUID studentId);

    record StudentSnapshot(UUID id, UUID programId, String status) {
        public boolean isActive() {
            return "ACTIVE".equals(status);
        }
    }
}
