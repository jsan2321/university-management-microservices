package com.panadi.ums.studentservice.application.port.in;

import com.panadi.ums.studentservice.application.PageResult;
import com.panadi.ums.studentservice.application.command.StudentCommand;
import com.panadi.ums.studentservice.domain.model.Student;
import com.panadi.ums.studentservice.domain.model.StudentStatus;

import java.util.UUID;

public interface StudentUseCase {
    Student createStudent(StudentCommand command);
    Student updateStudent(UUID id, StudentCommand command);
    Student getStudent(UUID id);
    PageResult<Student> listStudents(StudentStatus status, UUID programId, int page, int size);
    Student activateStudent(UUID id);
    Student deactivateStudent(UUID id);
    Student suspendStudent(UUID id);
}
