package com.panadi.ums.academicservice.application.service;

import com.panadi.ums.academicservice.application.ApplicationException;
import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.application.ResourceNotFoundException;
import com.panadi.ums.academicservice.application.command.Commands.DepartmentCommand;
import com.panadi.ums.academicservice.application.command.Commands.ProgramCommand;
import com.panadi.ums.academicservice.application.command.Commands.ScheduleCommand;
import com.panadi.ums.academicservice.application.command.Commands.SectionCommand;
import com.panadi.ums.academicservice.application.command.Commands.SemesterCommand;
import com.panadi.ums.academicservice.application.command.Commands.SubjectCommand;
import com.panadi.ums.academicservice.application.command.Commands.TeacherCommand;
import com.panadi.ums.academicservice.application.port.in.AcademicCatalogUseCase;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AcademicCatalogService implements AcademicCatalogUseCase {
    private final DepartmentRepositoryPort departments;
    private final ProgramRepositoryPort programs;
    private final TeacherRepositoryPort teachers;
    private final SemesterRepositoryPort semesters;
    private final SubjectRepositoryPort subjects;
    private final SectionRepositoryPort sections;

    public AcademicCatalogService(
            DepartmentRepositoryPort departments,
            ProgramRepositoryPort programs,
            TeacherRepositoryPort teachers,
            SemesterRepositoryPort semesters,
            SubjectRepositoryPort subjects,
            SectionRepositoryPort sections
    ) {
        this.departments = departments;
        this.programs = programs;
        this.teachers = teachers;
        this.semesters = semesters;
        this.subjects = subjects;
        this.sections = sections;
    }

    @Override
    public Department createDepartment(DepartmentCommand command) {
        ensureUniqueDepartmentCode(command.code(), null);
        return departments.saveDepartment(Department.create(command.code(), command.name(), command.description()));
    }

    @Override
    public Department updateDepartment(UUID id, DepartmentCommand command) {
        Department current = getDepartment(id);
        ensureUniqueDepartmentCode(command.code(), id);
        return departments.saveDepartment(current.update(command.code(), command.name(), command.description()));
    }

    @Override
    public Department getDepartment(UUID id) {
        return departments.findDepartmentById(id).orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    @Override
    public PageResult<Department> listDepartments(AcademicStatus status, int page, int size) {
        return departments.findDepartments(status, page, size);
    }

    @Override
    public Department activateDepartment(UUID id) {
        return departments.saveDepartment(getDepartment(id).activate());
    }

    @Override
    public Department deactivateDepartment(UUID id) {
        return departments.saveDepartment(getDepartment(id).deactivate());
    }

    @Override
    public AcademicProgram createProgram(ProgramCommand command) {
        requireActiveDepartment(command.departmentId());
        ensureUniqueProgramCode(command.code(), null);
        return programs.saveProgram(AcademicProgram.create(command.departmentId(), command.code(), command.name(), command.durationSemesters(), command.totalCredits()));
    }

    @Override
    public AcademicProgram updateProgram(UUID id, ProgramCommand command) {
        AcademicProgram current = getProgram(id);
        requireActiveDepartment(command.departmentId());
        ensureUniqueProgramCode(command.code(), id);
        return programs.saveProgram(current.update(command.departmentId(), command.code(), command.name(), command.durationSemesters(), command.totalCredits()));
    }

    @Override
    public AcademicProgram getProgram(UUID id) {
        return programs.findProgramById(id).orElseThrow(() -> new ResourceNotFoundException("Program not found"));
    }

    @Override
    public PageResult<AcademicProgram> listPrograms(UUID departmentId, AcademicStatus status, int page, int size) {
        return programs.findPrograms(departmentId, status, page, size);
    }

    @Override
    public AcademicProgram activateProgram(UUID id) {
        return programs.saveProgram(getProgram(id).activate());
    }

    @Override
    public AcademicProgram deactivateProgram(UUID id) {
        return programs.saveProgram(getProgram(id).deactivate());
    }

    @Override
    public Teacher createTeacher(TeacherCommand command) {
        requireActiveDepartment(command.departmentId());
        ensureUniqueTeacher(command.teacherCode(), command.email(), null);
        ensureUniqueTeacherUser(command.userId(), null);
        return teachers.saveTeacher(Teacher.create(command.departmentId(), command.userId(), command.teacherCode(), command.firstName(), command.lastName(), command.email(), command.phone(), command.hireDate()));
    }

    @Override
    public Teacher updateTeacher(UUID id, TeacherCommand command) {
        Teacher current = getTeacher(id);
        requireActiveDepartment(command.departmentId());
        ensureUniqueTeacher(command.teacherCode(), command.email(), id);
        return teachers.saveTeacher(current.update(command.departmentId(), current.userId(), command.teacherCode(), command.firstName(), command.lastName(), command.email(), command.phone(), command.hireDate()));
    }

    @Override
    public Teacher getTeacher(UUID id) {
        return teachers.findTeacherById(id).orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
    }

    @Override
    public Teacher getTeacherByUserId(UUID userId) {
        return teachers.findTeacherByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for authenticated user"));
    }

    @Override
    public Teacher linkTeacherUser(UUID id, UUID userId) {
        Teacher current = getTeacher(id);
        ensureUniqueTeacherUser(userId, id);
        return teachers.saveTeacher(current.update(current.departmentId(), userId, current.teacherCode(), current.firstName(),
                current.lastName(), current.email(), current.phone(), current.hireDate()));
    }

    @Override
    public PageResult<Teacher> listTeachers(UUID departmentId, AcademicStatus status, int page, int size) {
        return teachers.findTeachers(departmentId, status, page, size);
    }

    @Override
    public Teacher activateTeacher(UUID id) {
        return teachers.saveTeacher(getTeacher(id).activate());
    }

    @Override
    public Teacher deactivateTeacher(UUID id) {
        return teachers.saveTeacher(getTeacher(id).deactivate());
    }

    @Override
    public Semester createSemester(SemesterCommand command) {
        ensureUniqueSemesterName(command.name(), null);
        return semesters.saveSemester(Semester.create(command.name(), command.startDate(), command.endDate()));
    }

    @Override
    public Semester updateSemester(UUID id, SemesterCommand command) {
        Semester current = getSemester(id);
        ensureUniqueSemesterName(command.name(), id);
        return semesters.saveSemester(current.update(command.name(), command.startDate(), command.endDate()));
    }

    @Override
    public Semester getSemester(UUID id) {
        return semesters.findSemesterById(id).orElseThrow(() -> new ResourceNotFoundException("Semester not found"));
    }

    @Override
    public PageResult<Semester> listSemesters(SemesterStatus status, int page, int size) {
        return semesters.findSemesters(status, page, size);
    }

    @Override
    public Semester activateSemester(UUID id) {
        return semesters.saveSemester(getSemester(id).activate());
    }

    @Override
    public Semester deactivateSemester(UUID id) {
        return semesters.saveSemester(getSemester(id).deactivate());
    }

    @Override
    public Subject createSubject(SubjectCommand command) {
        requireActiveProgram(command.programId());
        ensureUniqueSubjectCode(command.code(), null);
        ensureValidPrerequisites(null, command.prerequisiteSubjectIds());
        return subjects.saveSubject(Subject.create(command.programId(), command.code(), command.name(), command.description(), command.credits(), command.minimumCreditsRequired(), command.prerequisiteSubjectIds()));
    }

    @Override
    public Subject updateSubject(UUID id, SubjectCommand command) {
        Subject current = getSubject(id);
        requireActiveProgram(command.programId());
        ensureUniqueSubjectCode(command.code(), id);
        ensureValidPrerequisites(id, command.prerequisiteSubjectIds());
        return subjects.saveSubject(current.update(command.programId(), command.code(), command.name(), command.description(), command.credits(), command.minimumCreditsRequired(), command.prerequisiteSubjectIds()));
    }

    @Override
    public Subject getSubject(UUID id) {
        return subjects.findSubjectById(id).orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }

    @Override
    public PageResult<Subject> listSubjects(UUID programId, AcademicStatus status, int page, int size) {
        return subjects.findSubjects(programId, status, page, size);
    }

    @Override
    public Subject activateSubject(UUID id) {
        return subjects.saveSubject(getSubject(id).activate());
    }

    @Override
    public Subject deactivateSubject(UUID id) {
        return subjects.saveSubject(getSubject(id).deactivate());
    }

    @Override
    public Section createSection(SectionCommand command) {
        requireActiveSubject(command.subjectId());
        requireActiveTeacher(command.teacherId());
        requireActiveSemester(command.semesterId());
        ensureUniqueSectionCode(command.sectionCode(), null);
        return sections.saveSection(Section.create(command.subjectId(), command.teacherId(), command.semesterId(), command.sectionCode(), command.capacity(), toSchedules(command.schedules())));
    }

    @Override
    public Section updateSection(UUID id, SectionCommand command) {
        Section current = getSection(id);
        requireActiveSubject(command.subjectId());
        requireActiveTeacher(command.teacherId());
        requireActiveSemester(command.semesterId());
        ensureUniqueSectionCode(command.sectionCode(), id);
        return sections.saveSection(current.update(command.subjectId(), command.teacherId(), command.semesterId(), command.sectionCode(), command.capacity(), toSchedules(command.schedules())));
    }

    @Override
    public Section getSection(UUID id) {
        return sections.findSectionById(id).orElseThrow(() -> new ResourceNotFoundException("Section not found"));
    }

    @Override
    public PageResult<Section> listSections(UUID subjectId, UUID teacherId, UUID semesterId, AcademicStatus status, int page, int size) {
        return sections.findSections(subjectId, teacherId, semesterId, status, page, size);
    }

    @Override
    public Section activateSection(UUID id) {
        return sections.saveSection(getSection(id).activate());
    }

    @Override
    public Section deactivateSection(UUID id) {
        return sections.saveSection(getSection(id).deactivate());
    }

    private void ensureUniqueDepartmentCode(String code, UUID excludedId) {
        if (departments.existsDepartmentByCode(code, excludedId)) {
            throw new ApplicationException("Department code already exists");
        }
    }

    private void ensureUniqueProgramCode(String code, UUID excludedId) {
        if (programs.existsProgramByCode(code, excludedId)) {
            throw new ApplicationException("Program code already exists");
        }
    }

    private void ensureUniqueTeacher(String teacherCode, String email, UUID excludedId) {
        if (teachers.existsByTeacherCode(teacherCode, excludedId)) {
            throw new ApplicationException("Teacher code already exists");
        }
        if (teachers.existsByEmail(email, excludedId)) {
            throw new ApplicationException("Teacher email already exists");
        }
    }

    private void ensureUniqueTeacherUser(UUID userId, UUID excludedId) {
        if (teachers.existsByUserId(userId, excludedId)) {
            throw new ApplicationException("Keycloak user is already linked to another teacher profile");
        }
    }

    private void ensureUniqueSemesterName(String name, UUID excludedId) {
        if (semesters.existsSemesterByName(name, excludedId)) {
            throw new ApplicationException("Semester name already exists");
        }
    }

    private void ensureUniqueSubjectCode(String code, UUID excludedId) {
        if (subjects.existsSubjectByCode(code, excludedId)) {
            throw new ApplicationException("Subject code already exists");
        }
    }

    private void ensureUniqueSectionCode(String code, UUID excludedId) {
        if (sections.existsBySectionCode(code, excludedId)) {
            throw new ApplicationException("Section code already exists");
        }
    }

    private void ensureValidPrerequisites(UUID subjectId, Set<UUID> prerequisiteIds) {
        if (prerequisiteIds == null || prerequisiteIds.isEmpty()) {
            return;
        }
        if (subjectId != null && prerequisiteIds.contains(subjectId)) {
            throw new ApplicationException("Subject cannot be its own prerequisite");
        }
        if (!subjects.allSubjectsExist(prerequisiteIds)) {
            throw new ApplicationException("One or more prerequisite subjects do not exist");
        }
    }

    private void requireActiveDepartment(UUID id) {
        Department department = getDepartment(id);
        if (department.status() != AcademicStatus.ACTIVE) {
            throw new ApplicationException("Department is inactive");
        }
    }

    private void requireActiveProgram(UUID id) {
        AcademicProgram program = getProgram(id);
        if (program.status() != AcademicStatus.ACTIVE) {
            throw new ApplicationException("Program is inactive");
        }
    }

    private void requireActiveTeacher(UUID id) {
        Teacher teacher = getTeacher(id);
        if (teacher.status() != AcademicStatus.ACTIVE) {
            throw new ApplicationException("Teacher is inactive");
        }
    }

    private void requireActiveSubject(UUID id) {
        Subject subject = getSubject(id);
        if (subject.status() != AcademicStatus.ACTIVE) {
            throw new ApplicationException("Subject is inactive");
        }
    }

    private void requireActiveSemester(UUID id) {
        Semester semester = getSemester(id);
        if (semester.status() != SemesterStatus.ACTIVE) {
            throw new ApplicationException("Semester is not active");
        }
    }

    private List<SectionSchedule> toSchedules(List<ScheduleCommand> schedules) {
        if (schedules == null) {
            return List.of();
        }
        return schedules.stream()
                .map(schedule -> new SectionSchedule(null, schedule.dayOfWeek(), schedule.startTime(), schedule.endTime()))
                .toList();
    }
}
