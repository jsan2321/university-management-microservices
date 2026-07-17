package com.panadi.ums.academicservice.infrastructure.config;

import com.panadi.ums.academicservice.application.port.out.DepartmentRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.ProgramRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SectionRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SemesterRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SubjectRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.TeacherRepositoryPort;
import com.panadi.ums.academicservice.application.service.AcademicCatalogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AcademicApplicationConfig {
    @Bean
    AcademicCatalogService academicCatalogService(
            DepartmentRepositoryPort departments,
            ProgramRepositoryPort programs,
            TeacherRepositoryPort teachers,
            SemesterRepositoryPort semesters,
            SubjectRepositoryPort subjects,
            SectionRepositoryPort sections
    ) {
        return new AcademicCatalogService(departments, programs, teachers, semesters, subjects, sections);
    }
}
