package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.academic;

import com.panadi.ums.enrollmentservice.application.ApplicationException;
import com.panadi.ums.enrollmentservice.application.DependencyUnavailableException;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class AcademicCatalogLookupAdapter implements AcademicCatalogLookupPort {
    private final AcademicClient client;

    AcademicCatalogLookupAdapter(AcademicClient client) {
        this.client = client;
    }

    @Override
    public SemesterSnapshot getSemester(UUID semesterId) {
        try {
            AcademicClient.SemesterResponse response = client.getSemester(semesterId);
            return new SemesterSnapshot(response.id(), response.status());
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Semester does not exist");
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }

    @Override
    public SectionSnapshot getSection(UUID sectionId) {
        try {
            AcademicClient.SectionResponse response = client.getSection(sectionId);
            return new SectionSnapshot(
                    response.id(),
                    response.subjectId(),
                    response.teacherId(),
                    response.semesterId(),
                    response.capacity(),
                    response.status(),
                    response.schedules() == null ? java.util.List.of() : response.schedules().stream()
                            .map(schedule -> new ScheduleSnapshot(schedule.dayOfWeek(), schedule.startTime(), schedule.endTime()))
                            .toList()
            );
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Section does not exist");
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }

    @Override
    public SubjectSnapshot getSubject(UUID subjectId) {
        try {
            AcademicClient.SubjectResponse response = client.getSubject(subjectId);
            return new SubjectSnapshot(response.id(), response.programId(), response.credits(), response.prerequisiteSubjectIds(), response.status());
        } catch (FeignException.NotFound exception) {
            throw new ApplicationException("Subject does not exist");
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Academic Service is unavailable");
        }
    }
}
