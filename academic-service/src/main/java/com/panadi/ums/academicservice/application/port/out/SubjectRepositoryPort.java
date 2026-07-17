package com.panadi.ums.academicservice.application.port.out;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.Subject;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SubjectRepositoryPort {
    Subject saveSubject(Subject subject);
    Optional<Subject> findSubjectById(UUID id);
    PageResult<Subject> findSubjects(UUID programId, AcademicStatus status, int page, int size);
    boolean existsSubjectByCode(String code, UUID excludedId);
    boolean allSubjectsExist(Set<UUID> ids);
}
