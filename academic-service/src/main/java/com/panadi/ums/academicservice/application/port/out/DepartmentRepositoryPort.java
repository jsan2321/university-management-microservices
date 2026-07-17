package com.panadi.ums.academicservice.application.port.out;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.Department;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepositoryPort {
    Department saveDepartment(Department department);
    Optional<Department> findDepartmentById(UUID id);
    PageResult<Department> findDepartments(AcademicStatus status, int page, int size);
    boolean existsDepartmentByCode(String code, UUID excludedId);
}
