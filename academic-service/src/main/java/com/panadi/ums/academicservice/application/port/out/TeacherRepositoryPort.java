package com.panadi.ums.academicservice.application.port.out;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.Teacher;
import java.util.Optional;
import java.util.UUID;

public interface TeacherRepositoryPort {
    Teacher saveTeacher(Teacher teacher);
    Optional<Teacher> findTeacherById(UUID id);
    PageResult<Teacher> findTeachers(UUID departmentId, AcademicStatus status, int page, int size);
    boolean existsByTeacherCode(String teacherCode, UUID excludedId);
    boolean existsByEmail(String email, UUID excludedId);
}
