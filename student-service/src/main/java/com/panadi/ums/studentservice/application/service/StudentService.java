package com.panadi.ums.studentservice.application.service;

import com.panadi.ums.studentservice.application.ApplicationException;
import com.panadi.ums.studentservice.application.PageResult;
import com.panadi.ums.studentservice.application.ResourceNotFoundException;
import com.panadi.ums.studentservice.application.command.StudentCommand;
import com.panadi.ums.studentservice.application.port.in.StudentUseCase;
import com.panadi.ums.studentservice.application.port.out.AcademicProgramValidationPort;
import com.panadi.ums.studentservice.application.port.out.StudentRepositoryPort;
import com.panadi.ums.studentservice.domain.model.Student;
import com.panadi.ums.studentservice.domain.model.StudentStatus;

import java.util.UUID;

public class StudentService implements StudentUseCase {
    private final StudentRepositoryPort students;
    private final AcademicProgramValidationPort academicPrograms;

    public StudentService(StudentRepositoryPort students, AcademicProgramValidationPort academicPrograms) {
        this.students = students;
        this.academicPrograms = academicPrograms;
    }

    @Override
    public Student createStudent(StudentCommand command) {
        academicPrograms.validateActiveProgram(command.programId());
        ensureUniqueStudentCode(command.studentCode(), null);
        ensureUniqueEmail(command.email(), null);
        ensureUniqueStudentUser(command.userId(), null);
        return students.saveStudent(Student.create(command.userId(), command.studentCode(), command.firstName(), command.lastName(), command.gender(), command.dateOfBirth(), command.email(), command.phone(), command.address(), command.programId(), command.admissionDate()));
    }

    @Override
    public Student updateStudent(UUID id, StudentCommand command) {
        Student current = getStudent(id);
        academicPrograms.validateActiveProgram(command.programId());
        ensureUniqueStudentCode(command.studentCode(), id);
        ensureUniqueEmail(command.email(), id);
        return students.saveStudent(current.update(current.userId(), command.studentCode(), command.firstName(), command.lastName(), command.gender(), command.dateOfBirth(), command.email(), command.phone(), command.address(), command.programId(), command.admissionDate()));
    }

    @Override
    public Student getStudent(UUID id) {
        return students.findStudentById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    @Override
    public Student getStudentByUserId(UUID userId) {
        return students.findStudentByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Student profile not found for authenticated user"));
    }

    @Override
    public Student linkStudentUser(UUID id, UUID userId) {
        Student current = getStudent(id);
        ensureUniqueStudentUser(userId, id);
        return students.saveStudent(current.update(userId, current.studentCode(), current.firstName(), current.lastName(),
                current.gender(), current.dateOfBirth(), current.email(), current.phone(), current.address(),
                current.programId(), current.admissionDate()));
    }

    @Override
    public PageResult<Student> listStudents(StudentStatus status, UUID programId, int page, int size) {
        return students.findStudents(status, programId, page, size);
    }

    @Override
    public Student activateStudent(UUID id) {
        return students.saveStudent(getStudent(id).activate());
    }

    @Override
    public Student deactivateStudent(UUID id) {
        return students.saveStudent(getStudent(id).deactivate());
    }

    @Override
    public Student suspendStudent(UUID id) {
        return students.saveStudent(getStudent(id).suspend());
    }

    private void ensureUniqueStudentCode(String studentCode, UUID excludedId) {
        if (students.existsByStudentCode(studentCode, excludedId)) {
            throw new ApplicationException("Student code already exists");
        }
    }

    private void ensureUniqueEmail(String email, UUID excludedId) {
        if (students.existsByEmail(email, excludedId)) {
            throw new ApplicationException("Student email already exists");
        }
    }

    private void ensureUniqueStudentUser(UUID userId, UUID excludedId) {
        if (students.existsByUserId(userId, excludedId)) {
            throw new ApplicationException("Keycloak user is already linked to another student profile");
        }
    }
}
