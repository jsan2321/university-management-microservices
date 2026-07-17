package com.panadi.ums.academicservice.application.port.out;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.domain.model.AcademicProgram;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import java.util.Optional;
import java.util.UUID;

public interface ProgramRepositoryPort {
    AcademicProgram saveProgram(AcademicProgram program);
    Optional<AcademicProgram> findProgramById(UUID id);
    PageResult<AcademicProgram> findPrograms(UUID departmentId, AcademicStatus status, int page, int size);
    boolean existsProgramByCode(String code, UUID excludedId);
}
