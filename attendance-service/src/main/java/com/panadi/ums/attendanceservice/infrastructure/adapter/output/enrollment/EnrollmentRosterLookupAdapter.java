package com.panadi.ums.attendanceservice.infrastructure.adapter.output.enrollment;

import com.panadi.ums.attendanceservice.application.DependencyUnavailableException;
import com.panadi.ums.attendanceservice.application.port.out.EnrollmentRosterLookupPort;
import feign.FeignException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
class EnrollmentRosterLookupAdapter implements EnrollmentRosterLookupPort {
    private final EnrollmentRosterClient client;
    private final CircuitBreaker breaker;
    private final Cache<UUID, Set<UUID>> cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1_000).build();

    EnrollmentRosterLookupAdapter(EnrollmentRosterClient client, CircuitBreakerRegistry registry) {
        this.client = client; this.breaker = registry.circuitBreaker("enrollment-service");
    }

    @Override
    public Set<UUID> getActiveStudentIdsBySection(UUID sectionId) {
        try {
            Set<UUID> value = CircuitBreaker.decorateSupplier(breaker, () -> {
                EnrollmentRosterClient.SectionStudentsResponse response = client.getActiveStudents(sectionId);
                return response.studentIds() == null ? Set.<UUID>of() : new HashSet<>(response.studentIds());
            }).get();
            cache.put(sectionId, value); return value;
        } catch (RuntimeException exception) {
            Set<UUID> cached = isReadRequest() ? cache.getIfPresent(sectionId) : null;
            if (cached != null) return cached;
            throw new DependencyUnavailableException("Enrollment Service is unavailable");
        }
    }
    private boolean isReadRequest() { return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a && "GET".equalsIgnoreCase(a.getRequest().getMethod()); }
}
