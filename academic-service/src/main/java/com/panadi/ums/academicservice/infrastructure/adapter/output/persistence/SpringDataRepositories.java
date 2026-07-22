package com.panadi.ums.academicservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.DepartmentEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.ProgramEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SectionEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SemesterEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SubjectEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SubjectPrerequisiteEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface DepartmentJpaRepository extends JpaRepository<DepartmentEntity, UUID>, JpaSpecificationExecutor<DepartmentEntity> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
}

interface ProgramJpaRepository extends JpaRepository<ProgramEntity, UUID>, JpaSpecificationExecutor<ProgramEntity> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
}

interface TeacherJpaRepository extends JpaRepository<TeacherEntity, UUID>, JpaSpecificationExecutor<TeacherEntity> {
    java.util.Optional<TeacherEntity> findByUserId(UUID userId);
    boolean existsByTeacherCode(String teacherCode);
    boolean existsByTeacherCodeAndIdNot(String teacherCode, UUID id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByUserId(UUID userId);
    boolean existsByUserIdAndIdNot(UUID userId, UUID id);
}

interface SemesterJpaRepository extends JpaRepository<SemesterEntity, UUID>, JpaSpecificationExecutor<SemesterEntity> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
}

interface SubjectJpaRepository extends JpaRepository<SubjectEntity, UUID>, JpaSpecificationExecutor<SubjectEntity> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    long countByIdIn(Collection<UUID> ids);
}

interface SubjectPrerequisiteJpaRepository extends JpaRepository<SubjectPrerequisiteEntity, UUID> {
    List<SubjectPrerequisiteEntity> findBySubjectId(UUID subjectId);
    void deleteBySubjectId(UUID subjectId);
}

interface SectionJpaRepository extends JpaRepository<SectionEntity, UUID>, JpaSpecificationExecutor<SectionEntity> {
    boolean existsBySectionCode(String sectionCode);
    boolean existsBySectionCodeAndIdNot(String sectionCode, UUID id);
}
