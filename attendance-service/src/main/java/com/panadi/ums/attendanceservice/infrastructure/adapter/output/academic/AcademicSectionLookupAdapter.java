package com.panadi.ums.attendanceservice.infrastructure.adapter.output.academic;

import com.panadi.ums.attendanceservice.application.ApplicationException;
import com.panadi.ums.attendanceservice.application.DependencyUnavailableException;
import com.panadi.ums.attendanceservice.application.port.out.AcademicSectionLookupPort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class AcademicSectionLookupAdapter implements AcademicSectionLookupPort {
    private final AcademicSectionClient client;
    private final CircuitBreaker breaker;

    AcademicSectionLookupAdapter(AcademicSectionClient client, CircuitBreakerRegistry registry) {
        this.client = client; this.breaker = registry.circuitBreaker("academic-service");
    }

    @Override
    public SectionSnapshot getSection(UUID sectionId) {
        try {
            AcademicSectionClient.SectionResponse response = CircuitBreaker.decorateSupplier(breaker, () -> client.getSection(sectionId)).get();
            return new SectionSnapshot(response.id(), response.teacherId(), response.status());
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Section does not exist");
        } catch (RuntimeException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }
}
