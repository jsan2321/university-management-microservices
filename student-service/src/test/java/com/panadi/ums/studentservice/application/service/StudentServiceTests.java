package com.panadi.ums.studentservice.application.service;

import com.panadi.ums.studentservice.application.ApplicationException;
import com.panadi.ums.studentservice.application.DependencyUnavailableException;
import com.panadi.ums.studentservice.application.command.StudentCommand;
import com.panadi.ums.studentservice.application.port.out.AcademicProgramValidationPort;
import com.panadi.ums.studentservice.application.port.out.StudentRepositoryPort;
import com.panadi.ums.studentservice.domain.model.Gender;
import com.panadi.ums.studentservice.domain.model.Student;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentServiceTests {
    private final UUID programId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final StudentCommand command = new StudentCommand(userId, "2026-0001", "Ada", "Lovelace", Gender.FEMALE, LocalDate.now().minusYears(20), "ada@example.com", null, null, programId, LocalDate.now());

    @Test
    void createsStudentWhenProgramIsActiveAndValuesAreUnique() {
        StudentRepositoryPort students = mock(StudentRepositoryPort.class);
        AcademicProgramValidationPort academic = mock(AcademicProgramValidationPort.class);
        when(students.saveStudent(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Student created = new StudentService(students, academic).createStudent(command);

        assertThat(created.studentCode()).isEqualTo("2026-0001");
        verify(academic).validateActiveProgram(programId);
    }

    @Test
    void rejectsDuplicateStudentCode() {
        StudentRepositoryPort students = mock(StudentRepositoryPort.class);
        when(students.existsByStudentCode("2026-0001", null)).thenReturn(true);

        assertThatThrownBy(() -> new StudentService(students, mock(AcademicProgramValidationPort.class)).createStudent(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("code");
    }

    @Test
    void rejectsDuplicateEmail() {
        StudentRepositoryPort students = mock(StudentRepositoryPort.class);
        when(students.existsByEmail("ada@example.com", null)).thenReturn(true);

        assertThatThrownBy(() -> new StudentService(students, mock(AcademicProgramValidationPort.class)).createStudent(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("email");
    }

    @Test
    void rejectsKeycloakUserAlreadyLinkedToAnotherStudent() {
        StudentRepositoryPort students = mock(StudentRepositoryPort.class);
        when(students.existsByUserId(userId, null)).thenReturn(true);

        assertThatThrownBy(() -> new StudentService(students, mock(AcademicProgramValidationPort.class)).createStudent(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("already linked");
    }

    @Test
    void rejectsInactiveOrMissingProgram() {
        AcademicProgramValidationPort academic = mock(AcademicProgramValidationPort.class);
        doThrow(new ApplicationException("Program is inactive")).when(academic).validateActiveProgram(programId);

        assertThatThrownBy(() -> new StudentService(mock(StudentRepositoryPort.class), academic).createStudent(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void failsWhenAcademicServiceIsUnavailable() {
        AcademicProgramValidationPort academic = mock(AcademicProgramValidationPort.class);
        doThrow(new DependencyUnavailableException("Academic Service is unavailable")).when(academic).validateActiveProgram(programId);

        assertThatThrownBy(() -> new StudentService(mock(StudentRepositoryPort.class), academic).createStudent(command))
                .isInstanceOf(DependencyUnavailableException.class)
                .hasMessageContaining("unavailable");
    }
}
