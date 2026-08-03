package com.panadi.ums.studentservice.infrastructure.adapter.output.academic;

import com.panadi.ums.studentservice.application.ApplicationException;
import com.panadi.ums.studentservice.application.DependencyUnavailableException;
import com.panadi.ums.studentservice.application.port.out.AcademicProgramValidationPort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class AcademicProgramValidationAdapter implements AcademicProgramValidationPort {
    private final AcademicProgramClient client;
    private final CircuitBreaker breaker;

    AcademicProgramValidationAdapter(AcademicProgramClient client, CircuitBreakerRegistry registry) {
        this.client = client; this.breaker = registry.circuitBreaker("academic-service");
    }

    @Override
    public void validateActiveProgram(UUID programId) {
        try {
            AcademicProgramClient.AcademicProgramResponse program = CircuitBreaker.decorateSupplier(breaker, () -> client.getProgram(programId)).get();
            if (!"ACTIVE".equals(program.status())) {
                throw new ApplicationException("Program is inactive");
            }
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Program does not exist");
        } catch (RuntimeException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }
}
