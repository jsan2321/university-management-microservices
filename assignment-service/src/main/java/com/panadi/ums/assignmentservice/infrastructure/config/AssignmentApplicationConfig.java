package com.panadi.ums.assignmentservice.infrastructure.config;

import com.panadi.ums.assignmentservice.application.port.out.AcademicSectionLookupPort;
import com.panadi.ums.assignmentservice.application.port.out.AssignmentRepositoryPort;
import com.panadi.ums.assignmentservice.application.port.out.EnrollmentRosterLookupPort;
import com.panadi.ums.assignmentservice.application.service.AssignmentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AssignmentApplicationConfig {
    @Bean
    AssignmentService assignmentService(AssignmentRepositoryPort repository, AcademicSectionLookupPort academicSections, EnrollmentRosterLookupPort enrollmentRoster) {
        return new AssignmentService(repository, academicSections, enrollmentRoster);
    }
}
