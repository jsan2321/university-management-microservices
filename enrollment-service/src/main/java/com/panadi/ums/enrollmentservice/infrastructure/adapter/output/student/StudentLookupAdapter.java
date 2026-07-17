package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.student;

import com.panadi.ums.enrollmentservice.application.ApplicationException;
import com.panadi.ums.enrollmentservice.application.DependencyUnavailableException;
import com.panadi.ums.enrollmentservice.application.port.out.StudentLookupPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class StudentLookupAdapter implements StudentLookupPort {
    private final StudentClient client;

    StudentLookupAdapter(StudentClient client) {
        this.client = client;
    }

    @Override
    public StudentSnapshot getStudent(UUID studentId) {
        try {
            StudentClient.StudentResponse response = client.getStudent(studentId);
            return new StudentSnapshot(response.id(), response.programId(), response.status());
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Student does not exist");
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Student Service is unavailable");
        }
    }
}
