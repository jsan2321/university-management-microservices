package com.panadi.ums.assignmentservice.infrastructure.adapter.output.academic;

import com.panadi.ums.assignmentservice.application.ApplicationException;
import com.panadi.ums.assignmentservice.application.DependencyUnavailableException;
import com.panadi.ums.assignmentservice.application.port.out.AcademicSectionLookupPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class AcademicSectionLookupAdapter implements AcademicSectionLookupPort {
    private final AcademicSectionClient client;

    AcademicSectionLookupAdapter(AcademicSectionClient client) { this.client = client; }

    @Override
    public SectionSnapshot getSection(UUID sectionId) {
        try {
            AcademicSectionClient.SectionResponse response = client.getSection(sectionId);
            return new SectionSnapshot(response.id(), response.teacherId(), response.status());
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Section does not exist");
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }
}
