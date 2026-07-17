package com.panadi.ums.academicservice.application.port.out;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.domain.model.Semester;
import com.panadi.ums.academicservice.domain.model.SemesterStatus;
import java.util.Optional;
import java.util.UUID;

public interface SemesterRepositoryPort {
    Semester saveSemester(Semester semester);
    Optional<Semester> findSemesterById(UUID id);
    PageResult<Semester> findSemesters(SemesterStatus status, int page, int size);
    boolean existsSemesterByName(String name, UUID excludedId);
}
