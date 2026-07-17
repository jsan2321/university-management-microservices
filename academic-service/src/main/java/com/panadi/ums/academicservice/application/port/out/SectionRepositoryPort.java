package com.panadi.ums.academicservice.application.port.out;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.Section;
import java.util.Optional;
import java.util.UUID;

public interface SectionRepositoryPort {
    Section saveSection(Section section);
    Optional<Section> findSectionById(UUID id);
    PageResult<Section> findSections(UUID subjectId, UUID teacherId, UUID semesterId, AcademicStatus status, int page, int size);
    boolean existsBySectionCode(String sectionCode, UUID excludedId);
}
