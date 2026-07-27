package com.panadi.ums.assignmentservice.infrastructure.adapter.output.enrollment;

import com.panadi.ums.assignmentservice.application.DependencyUnavailableException;
import com.panadi.ums.assignmentservice.application.port.out.EnrollmentRosterLookupPort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
class EnrollmentRosterLookupAdapter implements EnrollmentRosterLookupPort {
    private final EnrollmentRosterClient client;
    private final CircuitBreaker breaker;

    EnrollmentRosterLookupAdapter(EnrollmentRosterClient client, CircuitBreakerRegistry registry) { this.client = client; this.breaker = registry.circuitBreaker("enrollment-service"); }

    @Override
    public Set<UUID> getActiveStudentIdsBySection(UUID sectionId) {
        try {
            EnrollmentRosterClient.SectionStudentsResponse response = CircuitBreaker.decorateSupplier(breaker, () -> client.getActiveStudents(sectionId)).get();
            return response.studentIds() == null ? Set.of() : new HashSet<>(response.studentIds());
        } catch (RuntimeException exception) {
            throw new DependencyUnavailableException("Enrollment Service is unavailable");
        }
    }
}
