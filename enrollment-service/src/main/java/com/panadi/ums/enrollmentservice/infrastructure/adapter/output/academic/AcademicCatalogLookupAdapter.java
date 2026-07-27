package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.academic;

import com.panadi.ums.enrollmentservice.application.ApplicationException;
import com.panadi.ums.enrollmentservice.application.DependencyUnavailableException;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class AcademicCatalogLookupAdapter implements AcademicCatalogLookupPort {
    private final AcademicClient client;
    private final CircuitBreaker breaker;

    AcademicCatalogLookupAdapter(AcademicClient client, CircuitBreakerRegistry registry) {
        this.client = client;
        this.breaker = registry.circuitBreaker("academic-service");
    }

    @Override
    public SemesterSnapshot getSemester(UUID semesterId) {
        try {
            AcademicClient.SemesterResponse response = CircuitBreaker.decorateSupplier(breaker, () -> client.getSemester(semesterId)).get();
            return new SemesterSnapshot(response.id(), response.name(), response.status());
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Semester does not exist");
        } catch (RuntimeException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }

    @Override
    public SectionSnapshot getSection(UUID sectionId) {
        try {
            AcademicClient.SectionResponse response = CircuitBreaker.decorateSupplier(breaker, () -> client.getSection(sectionId)).get();
            return new SectionSnapshot(
                    response.id(),
                    response.subjectId(),
                    response.teacherId(),
                    response.semesterId(),
                    response.sectionCode(),
                    response.capacity(),
                    response.status(),
                    response.schedules() == null ? java.util.List.of() : response.schedules().stream()
                            .map(schedule -> new ScheduleSnapshot(schedule.dayOfWeek(), schedule.startTime(), schedule.endTime()))
                            .toList()
            );
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Section does not exist");
        } catch (RuntimeException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }

    @Override
    public SubjectSnapshot getSubject(UUID subjectId) {
        try {
            AcademicClient.SubjectResponse response = CircuitBreaker.decorateSupplier(breaker, () -> client.getSubject(subjectId)).get();
            return new SubjectSnapshot(response.id(), response.programId(), response.code(), response.name(), response.credits(), response.prerequisiteSubjectIds(), response.status());
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Subject does not exist");
        } catch (RuntimeException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }
}
