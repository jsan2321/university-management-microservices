package com.panadi.ums.studentservice.infrastructure.adapter.input.rest;

import com.panadi.ums.security.CurrentActor;
import com.panadi.ums.studentservice.application.command.StudentCommand;
import com.panadi.ums.studentservice.application.port.in.StudentUseCase;
import com.panadi.ums.studentservice.domain.model.Student;
import com.panadi.ums.studentservice.infrastructure.adapter.input.rest.dto.StudentDtos.StudentRequest;
import com.panadi.ums.studentservice.infrastructure.adapter.input.rest.dto.StudentDtos.StudentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class StudentIdentityController {
    private final StudentUseCase useCase;

    StudentIdentityController(StudentUseCase useCase) { this.useCase = useCase; }

    @GetMapping("/api/v1/students/me")
    @PreAuthorize("hasRole('STUDENT')")
    StudentResponse me() {
        return toResponse(useCase.getStudentByUserId(CurrentActor.required().userId()));
    }

    @PostMapping("/internal/students")
    @ResponseStatus(HttpStatus.CREATED)
    StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return toResponse(useCase.createStudent(new StudentCommand(request.userId(), request.studentCode(), request.firstName(),
                request.lastName(), request.gender(), request.dateOfBirth(), request.email(), request.phone(), request.address(),
                request.programId(), request.admissionDate())));
    }

    @PatchMapping("/internal/students/{profileId}/identity/{userId}")
    StudentResponse link(@PathVariable UUID profileId, @PathVariable UUID userId) {
        return toResponse(useCase.linkStudentUser(profileId, userId));
    }

    @GetMapping("/internal/students/{profileId}")
    StudentResponse internalById(@PathVariable UUID profileId) {
        return toResponse(useCase.getStudent(profileId));
    }

    @GetMapping("/internal/students/by-user/{userId}")
    StudentResponse internalByUser(@PathVariable UUID userId) {
        return toResponse(useCase.getStudentByUserId(userId));
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(student.id(), student.userId(), student.studentCode(), student.firstName(), student.lastName(),
                student.gender(), student.dateOfBirth(), student.email(), student.phone(), student.address(), student.programId(),
                student.admissionDate(), student.status(), student.createdAt(), student.updatedAt());
    }
}
