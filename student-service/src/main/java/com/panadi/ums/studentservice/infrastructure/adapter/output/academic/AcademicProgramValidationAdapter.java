package com.panadi.ums.studentservice.infrastructure.adapter.output.academic;

import com.panadi.ums.studentservice.application.ApplicationException;
import com.panadi.ums.studentservice.application.DependencyUnavailableException;
import com.panadi.ums.studentservice.application.port.out.AcademicProgramValidationPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class AcademicProgramValidationAdapter implements AcademicProgramValidationPort {
    private final AcademicProgramClient client;

    AcademicProgramValidationAdapter(AcademicProgramClient client) {
        this.client = client;
    }

    @Override
    public void validateActiveProgram(UUID programId) {
        try {
            AcademicProgramClient.AcademicProgramResponse program = client.getProgram(programId);
            if (!"ACTIVE".equals(program.status())) {
                throw new ApplicationException("Program is inactive");
            }
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Program does not exist");
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }
}
