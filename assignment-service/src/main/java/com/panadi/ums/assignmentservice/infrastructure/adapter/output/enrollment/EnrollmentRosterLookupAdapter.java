package com.panadi.ums.assignmentservice.infrastructure.adapter.output.enrollment;

import com.panadi.ums.assignmentservice.application.DependencyUnavailableException;
import com.panadi.ums.assignmentservice.application.port.out.EnrollmentRosterLookupPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
class EnrollmentRosterLookupAdapter implements EnrollmentRosterLookupPort {
    private final EnrollmentRosterClient client;

    EnrollmentRosterLookupAdapter(EnrollmentRosterClient client) { this.client = client; }

    @Override
    public Set<UUID> getActiveStudentIdsBySection(UUID sectionId) {
        try {
            EnrollmentRosterClient.SectionStudentsResponse response = client.getActiveStudents(sectionId);
            return response.studentIds() == null ? Set.of() : new HashSet<>(response.studentIds());
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Enrollment Service is unavailable");
        }
    }
}
