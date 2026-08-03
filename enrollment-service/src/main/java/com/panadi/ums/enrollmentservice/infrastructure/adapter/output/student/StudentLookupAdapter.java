package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.student;

import com.panadi.ums.enrollmentservice.application.ApplicationException;
import com.panadi.ums.enrollmentservice.application.DependencyUnavailableException;
import com.panadi.ums.enrollmentservice.application.port.out.StudentLookupPort;
import feign.FeignException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.UUID;

@Component
class StudentLookupAdapter implements StudentLookupPort {
    private final StudentClient client;
    private final CircuitBreaker breaker;
    private final Cache<UUID, StudentSnapshot> cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1_000).build();

    StudentLookupAdapter(StudentClient client, CircuitBreakerRegistry registry) {
        this.client = client;
        this.breaker = registry.circuitBreaker("student-service");
    }

    @Override
    public StudentSnapshot getStudent(UUID studentId) {
        try {
            StudentSnapshot value = CircuitBreaker.decorateSupplier(breaker, () -> {
                StudentClient.StudentResponse response = client.getStudent(studentId);
                return new StudentSnapshot(response.id(), response.programId(), response.status());
            }).get();
            cache.put(studentId, value);
            return value;
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Student does not exist");
        } catch (RuntimeException exception) {
            StudentSnapshot cached = isReadRequest() ? cache.getIfPresent(studentId) : null;
            if (cached != null) return cached;
            throw new DependencyUnavailableException("Student Service is unavailable");
        }
    }

    private boolean isReadRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                && "GET".equalsIgnoreCase(attributes.getRequest().getMethod());
    }
}
