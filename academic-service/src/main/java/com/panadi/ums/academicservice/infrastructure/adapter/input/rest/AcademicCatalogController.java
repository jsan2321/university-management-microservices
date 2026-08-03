package com.panadi.ums.academicservice.infrastructure.adapter.input.rest;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.application.command.Commands.DepartmentCommand;
import com.panadi.ums.academicservice.application.command.Commands.ProgramCommand;
import com.panadi.ums.academicservice.application.command.Commands.ScheduleCommand;
import com.panadi.ums.academicservice.application.command.Commands.SectionCommand;
import com.panadi.ums.academicservice.application.command.Commands.SemesterCommand;
import com.panadi.ums.academicservice.application.command.Commands.SubjectCommand;
import com.panadi.ums.academicservice.application.command.Commands.TeacherCommand;
import com.panadi.ums.academicservice.application.port.in.AcademicCatalogUseCase;
import com.panadi.ums.academicservice.domain.model.AcademicProgram;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.Department;
import com.panadi.ums.academicservice.domain.model.Section;
import com.panadi.ums.academicservice.domain.model.SectionSchedule;
import com.panadi.ums.academicservice.domain.model.Semester;
import com.panadi.ums.academicservice.domain.model.SemesterStatus;
import com.panadi.ums.academicservice.domain.model.Subject;
import com.panadi.ums.academicservice.domain.model.Teacher;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.DepartmentRequest;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.DepartmentResponse;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.PageResponse;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.ProgramRequest;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.ProgramResponse;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.ScheduleResponse;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.SectionRequest;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.SectionResponse;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.SemesterRequest;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.SemesterResponse;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.SubjectRequest;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.SubjectResponse;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.TeacherRequest;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.TeacherResponse;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.TeacherUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.panadi.ums.auditcommon.AuditOutbox;
import com.panadi.ums.security.CurrentActor;
import java.util.Map;


import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/academic")
class AcademicCatalogController {
    private final AcademicCatalogUseCase useCase;
    private final AuditOutbox audit;

    AcademicCatalogController(AcademicCatalogUseCase useCase, AuditOutbox audit) {
        this.useCase = useCase;
        this.audit = audit;
    }

    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    DepartmentResponse createDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse res = toResponse(useCase.createDepartment(new DepartmentCommand(request.code(), request.name(), request.description())));
        audit.record("DEPARTMENT_CREATED", "academic-service", "DEPARTMENT", res.id(), CurrentActor.required().userId(), Map.of("code", res.code(), "name", res.name()));
        return res;
    }

    @PutMapping("/departments/{id}")
    DepartmentResponse updateDepartment(@PathVariable UUID id, @Valid @RequestBody DepartmentRequest request) {
        return toResponse(useCase.updateDepartment(id, new DepartmentCommand(request.code(), request.name(), request.description())));
    }

    @GetMapping("/departments/{id}")
    DepartmentResponse getDepartment(@PathVariable UUID id) {
        return toResponse(useCase.getDepartment(id));
    }

    @GetMapping("/departments")
    PageResponse<DepartmentResponse> listDepartments(@RequestParam(required = false) AcademicStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return toPage(useCase.listDepartments(status, page, size), this::toResponse);
    }

    @PatchMapping("/departments/{id}/activate")
    DepartmentResponse activateDepartment(@PathVariable UUID id) {
        return toResponse(useCase.activateDepartment(id));
    }

    @PatchMapping("/departments/{id}/deactivate")
    DepartmentResponse deactivateDepartment(@PathVariable UUID id) {
        return toResponse(useCase.deactivateDepartment(id));
    }

    @PostMapping("/programs")
    @ResponseStatus(HttpStatus.CREATED)
    ProgramResponse createProgram(@Valid @RequestBody ProgramRequest request) {
        ProgramResponse res = toResponse(useCase.createProgram(new ProgramCommand(request.departmentId(), request.code(), request.name(), request.durationSemesters(), request.totalCredits())));
        audit.record("PROGRAM_CREATED", "academic-service", "PROGRAM", res.id(), CurrentActor.required().userId(), Map.of("code", res.code(), "name", res.name()));
        return res;
    }

    @PutMapping("/programs/{id}")
    ProgramResponse updateProgram(@PathVariable UUID id, @Valid @RequestBody ProgramRequest request) {
        return toResponse(useCase.updateProgram(id, new ProgramCommand(request.departmentId(), request.code(), request.name(), request.durationSemesters(), request.totalCredits())));
    }

    @GetMapping("/programs/{id}")
    ProgramResponse getProgram(@PathVariable UUID id) {
        return toResponse(useCase.getProgram(id));
    }

    @GetMapping("/programs")
    PageResponse<ProgramResponse> listPrograms(@RequestParam(required = false) UUID departmentId, @RequestParam(required = false) AcademicStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return toPage(useCase.listPrograms(departmentId, status, page, size), this::toResponse);
    }

    @PatchMapping("/programs/{id}/activate")
    ProgramResponse activateProgram(@PathVariable UUID id) {
        return toResponse(useCase.activateProgram(id));
    }

    @PatchMapping("/programs/{id}/deactivate")
    ProgramResponse deactivateProgram(@PathVariable UUID id) {
        return toResponse(useCase.deactivateProgram(id));
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    TeacherResponse createTeacher(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse res = toResponse(useCase.createTeacher(new TeacherCommand(request.departmentId(), request.userId(), request.teacherCode(), request.firstName(), request.lastName(), request.email(), request.phone(), request.hireDate())));
        audit.record("TEACHER_CREATED", "academic-service", "TEACHER", res.id(), CurrentActor.required().userId(), Map.of("teacherCode", res.teacherCode(), "email", res.email()));
        return res;
    }

    @PutMapping("/teachers/{id}")
    TeacherResponse updateTeacher(@PathVariable UUID id, @Valid @RequestBody TeacherUpdateRequest request) {
        return toResponse(useCase.updateTeacher(id, new TeacherCommand(request.departmentId(), null, request.teacherCode(), request.firstName(), request.lastName(), request.email(), request.phone(), request.hireDate())));
    }

    @GetMapping("/teachers/{id}")
    TeacherResponse getTeacher(@PathVariable UUID id) {
        return toResponse(useCase.getTeacher(id));
    }

    @GetMapping("/teachers")
    PageResponse<TeacherResponse> listTeachers(@RequestParam(required = false) UUID departmentId, @RequestParam(required = false) AcademicStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return toPage(useCase.listTeachers(departmentId, status, page, size), this::toResponse);
    }

    @PatchMapping("/teachers/{id}/activate")
    TeacherResponse activateTeacher(@PathVariable UUID id) {
        return toResponse(useCase.activateTeacher(id));
    }

    @PatchMapping("/teachers/{id}/deactivate")
    TeacherResponse deactivateTeacher(@PathVariable UUID id) {
        return toResponse(useCase.deactivateTeacher(id));
    }

    @PostMapping("/semesters")
    @ResponseStatus(HttpStatus.CREATED)
    SemesterResponse createSemester(@Valid @RequestBody SemesterRequest request) {
        SemesterResponse res = toResponse(useCase.createSemester(new SemesterCommand(request.name(), request.startDate(), request.endDate())));
        audit.record("SEMESTER_CREATED", "academic-service", "SEMESTER", res.id(), CurrentActor.required().userId(), Map.of("name", res.name()));
        return res;
    }

    @PutMapping("/semesters/{id}")
    SemesterResponse updateSemester(@PathVariable UUID id, @Valid @RequestBody SemesterRequest request) {
        return toResponse(useCase.updateSemester(id, new SemesterCommand(request.name(), request.startDate(), request.endDate())));
    }

    @GetMapping("/semesters/{id}")
    SemesterResponse getSemester(@PathVariable UUID id) {
        return toResponse(useCase.getSemester(id));
    }

    @GetMapping("/semesters")
    PageResponse<SemesterResponse> listSemesters(@RequestParam(required = false) SemesterStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return toPage(useCase.listSemesters(status, page, size), this::toResponse);
    }

    @PatchMapping("/semesters/{id}/activate")
    SemesterResponse activateSemester(@PathVariable UUID id) {
        return toResponse(useCase.activateSemester(id));
    }

    @PatchMapping("/semesters/{id}/deactivate")
    SemesterResponse deactivateSemester(@PathVariable UUID id) {
        return toResponse(useCase.deactivateSemester(id));
    }

    @PatchMapping("/semesters/{id}/toggle-registration")
    SemesterResponse toggleRegistration(@PathVariable UUID id, @RequestParam boolean open) {
        return toResponse(useCase.toggleRegistration(id, open));
    }

    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    SubjectResponse createSubject(@Valid @RequestBody SubjectRequest request) {
        SubjectResponse res = toResponse(useCase.createSubject(new SubjectCommand(request.programId(), request.code(), request.name(), request.description(), request.credits(), request.minimumCreditsRequired(), request.prerequisiteSubjectIds())));
        audit.record("SUBJECT_CREATED", "academic-service", "SUBJECT", res.id(), CurrentActor.required().userId(), Map.of("code", res.code(), "name", res.name()));
        return res;
    }

    @PutMapping("/subjects/{id}")
    SubjectResponse updateSubject(@PathVariable UUID id, @Valid @RequestBody SubjectRequest request) {
        return toResponse(useCase.updateSubject(id, new SubjectCommand(request.programId(), request.code(), request.name(), request.description(), request.credits(), request.minimumCreditsRequired(), request.prerequisiteSubjectIds())));
    }

    @GetMapping("/subjects/{id}")
    SubjectResponse getSubject(@PathVariable UUID id) {
        return toResponse(useCase.getSubject(id));
    }

    @GetMapping("/subjects")
    PageResponse<SubjectResponse> listSubjects(@RequestParam(required = false) UUID programId, @RequestParam(required = false) AcademicStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return toPage(useCase.listSubjects(programId, status, page, size), this::toResponse);
    }

    @PatchMapping("/subjects/{id}/activate")
    SubjectResponse activateSubject(@PathVariable UUID id) {
        return toResponse(useCase.activateSubject(id));
    }

    @PatchMapping("/subjects/{id}/deactivate")
    SubjectResponse deactivateSubject(@PathVariable UUID id) {
        return toResponse(useCase.deactivateSubject(id));
    }

    @PostMapping("/sections")
    @ResponseStatus(HttpStatus.CREATED)
    SectionResponse createSection(@Valid @RequestBody SectionRequest request) {
        SectionResponse res = toResponse(useCase.createSection(toCommand(request)));
        audit.record("SECTION_CREATED", "academic-service", "SECTION", res.id(), CurrentActor.required().userId(), Map.of("sectionCode", res.sectionCode()));
        return res;
    }

    @PutMapping("/sections/{id}")
    SectionResponse updateSection(@PathVariable UUID id, @Valid @RequestBody SectionRequest request) {
        return toResponse(useCase.updateSection(id, toCommand(request)));
    }

    @GetMapping("/sections/{id}")
    SectionResponse getSection(@PathVariable UUID id) {
        return toResponse(useCase.getSection(id));
    }

    @GetMapping("/sections")
    PageResponse<SectionResponse> listSections(@RequestParam(required = false) UUID subjectId, @RequestParam(required = false) UUID teacherId, @RequestParam(required = false) UUID semesterId, @RequestParam(required = false) AcademicStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return toPage(useCase.listSections(subjectId, teacherId, semesterId, status, page, size), this::toResponse);
    }

    @PatchMapping("/sections/{id}/activate")
    SectionResponse activateSection(@PathVariable UUID id) {
        return toResponse(useCase.activateSection(id));
    }

    @PatchMapping("/sections/{id}/deactivate")
    SectionResponse deactivateSection(@PathVariable UUID id) {
        return toResponse(useCase.deactivateSection(id));
    }

    private SectionCommand toCommand(SectionRequest request) {
        List<ScheduleCommand> schedules = request.schedules() == null ? List.of() : request.schedules().stream()
                .map(schedule -> new ScheduleCommand(schedule.dayOfWeek(), schedule.startTime(), schedule.endTime()))
                .toList();
        return new SectionCommand(request.subjectId(), request.teacherId(), request.semesterId(), request.sectionCode(), request.capacity(), schedules);
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(department.id(), department.code(), department.name(), department.description(), department.status(), department.createdAt(), department.updatedAt());
    }

    private ProgramResponse toResponse(AcademicProgram program) {
        return new ProgramResponse(program.id(), program.departmentId(), program.code(), program.name(), program.durationSemesters(), program.totalCredits(), program.status(), program.createdAt(), program.updatedAt());
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(teacher.id(), teacher.departmentId(), teacher.userId(), teacher.teacherCode(), teacher.firstName(), teacher.lastName(), teacher.email(), teacher.phone(), teacher.hireDate(), teacher.status(), teacher.createdAt(), teacher.updatedAt());
    }

    private SemesterResponse toResponse(Semester semester) {
        return new SemesterResponse(semester.id(), semester.name(), semester.startDate(), semester.endDate(), semester.status(), semester.isRegistrationOpen(), semester.createdAt(), semester.updatedAt());
    }

    private SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(subject.id(), subject.programId(), subject.code(), subject.name(), subject.description(), subject.credits(), subject.minimumCreditsRequired(), subject.prerequisiteSubjectIds(), subject.status(), subject.createdAt(), subject.updatedAt());
    }

    private SectionResponse toResponse(Section section) {
        return new SectionResponse(section.id(), section.subjectId(), section.teacherId(), section.semesterId(), section.sectionCode(), section.capacity(), section.schedules().stream().map(this::toResponse).toList(), section.status(), section.createdAt(), section.updatedAt());
    }

    private ScheduleResponse toResponse(SectionSchedule schedule) {
        return new ScheduleResponse(schedule.id(), schedule.dayOfWeek(), schedule.startTime(), schedule.endTime());
    }

    private <T, R> PageResponse<R> toPage(PageResult<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.content().stream().map(mapper).toList(), page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
