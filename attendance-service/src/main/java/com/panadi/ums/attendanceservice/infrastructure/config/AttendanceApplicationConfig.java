package com.panadi.ums.attendanceservice.infrastructure.config;

import com.panadi.ums.attendanceservice.application.port.out.AcademicSectionLookupPort;
import com.panadi.ums.attendanceservice.application.port.out.AttendanceRepositoryPort;
import com.panadi.ums.attendanceservice.application.port.out.EnrollmentRosterLookupPort;
import com.panadi.ums.attendanceservice.application.service.AttendanceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AttendanceApplicationConfig {
    @Bean
    AttendanceService attendanceService(AttendanceRepositoryPort attendance, AcademicSectionLookupPort academicSections, EnrollmentRosterLookupPort enrollmentRoster) {
        return new AttendanceService(attendance, academicSections, enrollmentRoster);
    }
}
