package com.panadi.ums.academicservice.application.service;

import com.panadi.ums.academicservice.application.ApplicationException;
import com.panadi.ums.academicservice.application.command.Commands.ProgramCommand;
import com.panadi.ums.academicservice.application.port.out.DepartmentRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.ProgramRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SectionRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SemesterRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SubjectRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.TeacherRepositoryPort;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.Department;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AcademicCatalogServiceTests {
    @Test
    void rejectsProgramCreationWhenDepartmentIsInactive() {
        UUID departmentId = UUID.randomUUID();
        DepartmentRepositoryPort departments = mock(DepartmentRepositoryPort.class);
        ProgramRepositoryPort programs = mock(ProgramRepositoryPort.class);

        when(departments.findDepartmentById(departmentId)).thenReturn(Optional.of(new Department(
                departmentId,
                "ENG",
                "Engineering",
                null,
                AcademicStatus.INACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        )));

        AcademicCatalogService service = new AcademicCatalogService(
                departments,
                programs,
                mock(TeacherRepositoryPort.class),
                mock(SemesterRepositoryPort.class),
                mock(SubjectRepositoryPort.class),
                mock(SectionRepositoryPort.class)
        );

        assertThatThrownBy(() -> service.createProgram(new ProgramCommand(departmentId, "SE", "Software Engineering", 10, 180)))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("inactive");
    }
}
