package com.panadi.ums.academicservice.infrastructure.adapter.input.rest;

import com.panadi.ums.academicservice.application.port.in.AcademicCatalogUseCase;
import com.panadi.ums.academicservice.domain.model.AcademicProgram;
import com.panadi.ums.academicservice.domain.model.Section;
import com.panadi.ums.academicservice.domain.model.Semester;
import com.panadi.ums.academicservice.domain.model.Subject;
import com.panadi.ums.academicservice.domain.model.Teacher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/academic")
class InternalAcademicController {
    private final AcademicCatalogUseCase useCase;

    InternalAcademicController(AcademicCatalogUseCase useCase) { this.useCase = useCase; }

    @GetMapping("/programs/{id}") AcademicProgram program(@PathVariable UUID id) { return useCase.getProgram(id); }
    @GetMapping("/semesters/{id}") Semester semester(@PathVariable UUID id) { return useCase.getSemester(id); }
    @GetMapping("/sections/{id}") Section section(@PathVariable UUID id) { return useCase.getSection(id); }
    @GetMapping("/subjects/{id}") Subject subject(@PathVariable UUID id) { return useCase.getSubject(id); }
    @GetMapping("/teachers/{id}") Teacher teacher(@PathVariable UUID id) { return useCase.getTeacher(id); }
    @GetMapping("/teachers/by-user/{userId}") Teacher teacherByUser(@PathVariable UUID userId) { return useCase.getTeacherByUserId(userId); }
}
