package com.panadi.ums.studentservice.application.port.out;

import com.panadi.ums.studentservice.application.PageResult;
import com.panadi.ums.studentservice.domain.model.Student;
import com.panadi.ums.studentservice.domain.model.StudentStatus;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepositoryPort {
    Student saveStudent(Student student);
    Optional<Student> findStudentById(UUID id);
    Optional<Student> findStudentByUserId(UUID userId);
    PageResult<Student> findStudents(StudentStatus status, UUID programId, int page, int size);
    boolean existsByStudentCode(String studentCode, UUID excludedId);
    boolean existsByEmail(String email, UUID excludedId);
    boolean existsByUserId(UUID userId, UUID excludedId);
}
