package com.panadi.ums.enrollmentservice.infrastructure.config;

import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort;
import com.panadi.ums.enrollmentservice.application.port.out.EnrollmentRepositoryPort;
import com.panadi.ums.enrollmentservice.application.port.out.StudentLookupPort;
import com.panadi.ums.enrollmentservice.application.service.EnrollmentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EnrollmentApplicationConfig {
    @Bean
    EnrollmentService enrollmentService(EnrollmentRepositoryPort enrollments, StudentLookupPort students, AcademicCatalogLookupPort academic) {
        return new EnrollmentService(enrollments, students, academic);
    }
}
