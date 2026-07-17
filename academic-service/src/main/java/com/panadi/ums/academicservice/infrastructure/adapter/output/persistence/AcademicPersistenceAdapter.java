package com.panadi.ums.academicservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.application.port.out.DepartmentRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.ProgramRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SectionRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SemesterRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.SubjectRepositoryPort;
import com.panadi.ums.academicservice.application.port.out.TeacherRepositoryPort;
import com.panadi.ums.academicservice.domain.model.AcademicProgram;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.Department;
import com.panadi.ums.academicservice.domain.model.Section;
import com.panadi.ums.academicservice.domain.model.SectionSchedule;
import com.panadi.ums.academicservice.domain.model.Semester;
import com.panadi.ums.academicservice.domain.model.SemesterStatus;
import com.panadi.ums.academicservice.domain.model.Subject;
import com.panadi.ums.academicservice.domain.model.Teacher;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.DepartmentEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.ProgramEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SectionEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SectionScheduleEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SemesterEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SubjectEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.SubjectPrerequisiteEntity;
import com.panadi.ums.academicservice.infrastructure.adapter.output.persistence.PersistenceEntities.TeacherEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Repository
@Transactional
class AcademicPersistenceAdapter implements DepartmentRepositoryPort, ProgramRepositoryPort, TeacherRepositoryPort, SemesterRepositoryPort, SubjectRepositoryPort, SectionRepositoryPort {
    private final DepartmentJpaRepository departments;
    private final ProgramJpaRepository programs;
    private final TeacherJpaRepository teachers;
    private final SemesterJpaRepository semesters;
    private final SubjectJpaRepository subjects;
    private final SubjectPrerequisiteJpaRepository subjectPrerequisites;
    private final SectionJpaRepository sections;

    AcademicPersistenceAdapter(
            DepartmentJpaRepository departments,
            ProgramJpaRepository programs,
            TeacherJpaRepository teachers,
            SemesterJpaRepository semesters,
            SubjectJpaRepository subjects,
            SubjectPrerequisiteJpaRepository subjectPrerequisites,
            SectionJpaRepository sections
    ) {
        this.departments = departments;
        this.programs = programs;
        this.teachers = teachers;
        this.semesters = semesters;
        this.subjects = subjects;
        this.subjectPrerequisites = subjectPrerequisites;
        this.sections = sections;
    }

    @Override
    public Department saveDepartment(Department department) {
        return toDomain(departments.save(toEntity(department)));
    }

    @Override
    public Optional<Department> findDepartmentById(UUID id) {
        return departments.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<Department> findDepartments(AcademicStatus status, int page, int size) {
        return toPage(departments.findAll(statusSpec(status), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsDepartmentByCode(String code, UUID excludedId) {
        return excludedId == null ? departments.existsByCode(code) : departments.existsByCodeAndIdNot(code, excludedId);
    }

    @Override
    public AcademicProgram saveProgram(AcademicProgram program) {
        return toDomain(programs.save(toEntity(program)));
    }

    @Override
    public Optional<AcademicProgram> findProgramById(UUID id) {
        return programs.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<AcademicProgram> findPrograms(UUID departmentId, AcademicStatus status, int page, int size) {
        return toPage(programs.findAll(programSpec(departmentId, status), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsProgramByCode(String code, UUID excludedId) {
        return excludedId == null ? programs.existsByCode(code) : programs.existsByCodeAndIdNot(code, excludedId);
    }

    @Override
    public Teacher saveTeacher(Teacher teacher) {
        return toDomain(teachers.save(toEntity(teacher)));
    }

    @Override
    public Optional<Teacher> findTeacherById(UUID id) {
        return teachers.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<Teacher> findTeachers(UUID departmentId, AcademicStatus status, int page, int size) {
        return toPage(teachers.findAll(teacherSpec(departmentId, status), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsByTeacherCode(String teacherCode, UUID excludedId) {
        return excludedId == null ? teachers.existsByTeacherCode(teacherCode) : teachers.existsByTeacherCodeAndIdNot(teacherCode, excludedId);
    }

    @Override
    public boolean existsByEmail(String email, UUID excludedId) {
        return excludedId == null ? teachers.existsByEmail(email) : teachers.existsByEmailAndIdNot(email, excludedId);
    }

    @Override
    public Semester saveSemester(Semester semester) {
        return toDomain(semesters.save(toEntity(semester)));
    }

    @Override
    public Optional<Semester> findSemesterById(UUID id) {
        return semesters.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<Semester> findSemesters(SemesterStatus status, int page, int size) {
        return toPage(semesters.findAll(semesterSpec(status), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsSemesterByName(String name, UUID excludedId) {
        return excludedId == null ? semesters.existsByName(name) : semesters.existsByNameAndIdNot(name, excludedId);
    }

    @Override
    public Subject saveSubject(Subject subject) {
        SubjectEntity saved = subjects.save(toEntity(subject));
        subjectPrerequisites.deleteBySubjectId(saved.id);
        subject.prerequisiteSubjectIds().forEach(prerequisiteId -> subjectPrerequisites.save(toPrerequisiteEntity(saved.id, prerequisiteId)));
        return toDomain(saved);
    }

    @Override
    public Optional<Subject> findSubjectById(UUID id) {
        return subjects.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<Subject> findSubjects(UUID programId, AcademicStatus status, int page, int size) {
        return toPage(subjects.findAll(subjectSpec(programId, status), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsSubjectByCode(String code, UUID excludedId) {
        return excludedId == null ? subjects.existsByCode(code) : subjects.existsByCodeAndIdNot(code, excludedId);
    }

    @Override
    public boolean allSubjectsExist(Set<UUID> ids) {
        return ids == null || ids.isEmpty() || subjects.countByIdIn(ids) == ids.size();
    }

    @Override
    public Section saveSection(Section section) {
        return toDomain(sections.save(toEntity(section)));
    }

    @Override
    public Optional<Section> findSectionById(UUID id) {
        return sections.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<Section> findSections(UUID subjectId, UUID teacherId, UUID semesterId, AcademicStatus status, int page, int size) {
        return toPage(sections.findAll(sectionSpec(subjectId, teacherId, semesterId, status), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsBySectionCode(String sectionCode, UUID excludedId) {
        return excludedId == null ? sections.existsBySectionCode(sectionCode) : sections.existsBySectionCodeAndIdNot(sectionCode, excludedId);
    }

    private DepartmentEntity toEntity(Department domain) {
        DepartmentEntity entity = new DepartmentEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.code = domain.code();
        entity.name = domain.name();
        entity.description = domain.description();
        entity.status = domain.status();
        entity.createdAt = domain.createdAt() == null ? LocalDateTime.now() : domain.createdAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        return entity;
    }

    private Department toDomain(DepartmentEntity entity) {
        return new Department(entity.id, entity.code, entity.name, entity.description, entity.status, entity.createdAt, entity.updatedAt);
    }

    private ProgramEntity toEntity(AcademicProgram domain) {
        ProgramEntity entity = new ProgramEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.departmentId = domain.departmentId();
        entity.code = domain.code();
        entity.name = domain.name();
        entity.durationSemesters = domain.durationSemesters();
        entity.totalCredits = domain.totalCredits();
        entity.status = domain.status();
        entity.createdAt = domain.createdAt() == null ? LocalDateTime.now() : domain.createdAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        return entity;
    }

    private AcademicProgram toDomain(ProgramEntity entity) {
        return new AcademicProgram(entity.id, entity.departmentId, entity.code, entity.name, entity.durationSemesters, entity.totalCredits, entity.status, entity.createdAt, entity.updatedAt);
    }

    private TeacherEntity toEntity(Teacher domain) {
        TeacherEntity entity = new TeacherEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.departmentId = domain.departmentId();
        entity.userId = domain.userId();
        entity.teacherCode = domain.teacherCode();
        entity.firstName = domain.firstName();
        entity.lastName = domain.lastName();
        entity.email = domain.email();
        entity.phone = domain.phone();
        entity.hireDate = domain.hireDate();
        entity.status = domain.status();
        entity.createdAt = domain.createdAt() == null ? LocalDateTime.now() : domain.createdAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        return entity;
    }

    private Teacher toDomain(TeacherEntity entity) {
        return new Teacher(entity.id, entity.departmentId, entity.userId, entity.teacherCode, entity.firstName, entity.lastName, entity.email, entity.phone, entity.hireDate, entity.status, entity.createdAt, entity.updatedAt);
    }

    private SemesterEntity toEntity(Semester domain) {
        SemesterEntity entity = new SemesterEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.name = domain.name();
        entity.startDate = domain.startDate();
        entity.endDate = domain.endDate();
        entity.status = domain.status();
        entity.createdAt = domain.createdAt() == null ? LocalDateTime.now() : domain.createdAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        return entity;
    }

    private Semester toDomain(SemesterEntity entity) {
        return new Semester(entity.id, entity.name, entity.startDate, entity.endDate, entity.status, entity.createdAt, entity.updatedAt);
    }

    private SubjectEntity toEntity(Subject domain) {
        SubjectEntity entity = new SubjectEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.programId = domain.programId();
        entity.code = domain.code();
        entity.name = domain.name();
        entity.description = domain.description();
        entity.credits = domain.credits();
        entity.minimumCreditsRequired = domain.minimumCreditsRequired();
        entity.status = domain.status();
        entity.createdAt = domain.createdAt() == null ? LocalDateTime.now() : domain.createdAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        return entity;
    }

    private Subject toDomain(SubjectEntity entity) {
        Set<UUID> prerequisiteIds = subjectPrerequisites.findBySubjectId(entity.id).stream()
                .map(prerequisite -> prerequisite.prerequisiteSubjectId)
                .collect(java.util.stream.Collectors.toSet());
        return new Subject(entity.id, entity.programId, entity.code, entity.name, entity.description, entity.credits, entity.minimumCreditsRequired, prerequisiteIds, entity.status, entity.createdAt, entity.updatedAt);
    }

    private SubjectPrerequisiteEntity toPrerequisiteEntity(UUID subjectId, UUID prerequisiteSubjectId) {
        SubjectPrerequisiteEntity entity = new SubjectPrerequisiteEntity();
        entity.id = UUID.randomUUID();
        entity.subjectId = subjectId;
        entity.prerequisiteSubjectId = prerequisiteSubjectId;
        entity.createdAt = LocalDateTime.now();
        return entity;
    }

    private SectionEntity toEntity(Section domain) {
        SectionEntity entity = new SectionEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.subjectId = domain.subjectId();
        entity.teacherId = domain.teacherId();
        entity.semesterId = domain.semesterId();
        entity.sectionCode = domain.sectionCode();
        entity.capacity = domain.capacity();
        entity.status = domain.status();
        entity.createdAt = domain.createdAt() == null ? LocalDateTime.now() : domain.createdAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        domain.schedules().forEach(schedule -> {
            SectionScheduleEntity scheduleEntity = new SectionScheduleEntity();
            scheduleEntity.id = schedule.id() == null ? UUID.randomUUID() : schedule.id();
            scheduleEntity.section = entity;
            scheduleEntity.dayOfWeek = schedule.dayOfWeek();
            scheduleEntity.startTime = schedule.startTime();
            scheduleEntity.endTime = schedule.endTime();
            scheduleEntity.createdAt = LocalDateTime.now();
            entity.schedules.add(scheduleEntity);
        });
        return entity;
    }

    private Section toDomain(SectionEntity entity) {
        List<SectionSchedule> schedules = entity.schedules.stream()
                .map(schedule -> new SectionSchedule(schedule.id, schedule.dayOfWeek, schedule.startTime, schedule.endTime))
                .toList();
        return new Section(entity.id, entity.subjectId, entity.teacherId, entity.semesterId, entity.sectionCode, entity.capacity, schedules, entity.status, entity.createdAt, entity.updatedAt);
    }

    private <T, R> PageResult<R> toPage(Page<T> page, Function<T, R> mapper) {
        return new PageResult<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private <T> Specification<T> statusSpec(AcademicStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    private Specification<ProgramEntity> programSpec(UUID departmentId, AcademicStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (departmentId != null) predicates.add(cb.equal(root.get("departmentId"), departmentId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<TeacherEntity> teacherSpec(UUID departmentId, AcademicStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (departmentId != null) predicates.add(cb.equal(root.get("departmentId"), departmentId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<SemesterEntity> semesterSpec(SemesterStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    private Specification<SubjectEntity> subjectSpec(UUID programId, AcademicStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (programId != null) predicates.add(cb.equal(root.get("programId"), programId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<SectionEntity> sectionSpec(UUID subjectId, UUID teacherId, UUID semesterId, AcademicStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (subjectId != null) predicates.add(cb.equal(root.get("subjectId"), subjectId));
            if (teacherId != null) predicates.add(cb.equal(root.get("teacherId"), teacherId));
            if (semesterId != null) predicates.add(cb.equal(root.get("semesterId"), semesterId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
