package com.panadi.ums.studentservice.infrastructure.config;

import com.panadi.ums.studentservice.application.port.out.AcademicProgramValidationPort;
import com.panadi.ums.studentservice.application.port.out.StudentRepositoryPort;
import com.panadi.ums.studentservice.application.service.StudentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class StudentApplicationConfig {
    @Bean
    StudentService studentService(StudentRepositoryPort students, AcademicProgramValidationPort academicPrograms) {
        return new StudentService(students, academicPrograms);
    }
}
