package com.panadi.ums.academicservice.infrastructure.adapter.input.rest;

import com.panadi.ums.academicservice.application.command.Commands.TeacherCommand;
import com.panadi.ums.academicservice.application.port.in.AcademicCatalogUseCase;
import com.panadi.ums.academicservice.domain.model.Teacher;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.TeacherRequest;
import com.panadi.ums.academicservice.infrastructure.adapter.input.rest.dto.AcademicDtos.TeacherResponse;
import com.panadi.ums.security.CurrentActor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class TeacherIdentityController {
    private final AcademicCatalogUseCase useCase;

    TeacherIdentityController(AcademicCatalogUseCase useCase) { this.useCase = useCase; }

    @GetMapping("/api/v1/academic/teachers/me")
    @PreAuthorize("hasRole('TEACHER')")
    TeacherResponse me() {
        return toResponse(useCase.getTeacherByUserId(CurrentActor.required().userId()));
    }

    @PostMapping("/internal/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    TeacherResponse create(@Valid @RequestBody TeacherRequest request) {
        return toResponse(useCase.createTeacher(new TeacherCommand(request.departmentId(), request.userId(), request.teacherCode(),
                request.firstName(), request.lastName(), request.email(), request.phone(), request.hireDate())));
    }

    @PatchMapping("/internal/teachers/{profileId}/identity/{userId}")
    TeacherResponse link(@PathVariable UUID profileId, @PathVariable UUID userId) {
        return toResponse(useCase.linkTeacherUser(profileId, userId));
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(teacher.id(), teacher.departmentId(), teacher.userId(), teacher.teacherCode(),
                teacher.firstName(), teacher.lastName(), teacher.email(), teacher.phone(), teacher.hireDate(),
                teacher.status(), teacher.createdAt(), teacher.updatedAt());
    }
}
