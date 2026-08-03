package com.panadi.ums.studentservice.infrastructure.adapter.input.rest;

import com.panadi.ums.studentservice.application.PageResult;
import com.panadi.ums.studentservice.application.command.StudentCommand;
import com.panadi.ums.studentservice.application.port.in.StudentUseCase;
import com.panadi.ums.studentservice.domain.model.Student;
import com.panadi.ums.studentservice.domain.model.StudentStatus;
import com.panadi.ums.studentservice.infrastructure.adapter.input.rest.dto.StudentDtos.PageResponse;
import com.panadi.ums.studentservice.infrastructure.adapter.input.rest.dto.StudentDtos.StudentRequest;
import com.panadi.ums.studentservice.infrastructure.adapter.input.rest.dto.StudentDtos.StudentResponse;
import com.panadi.ums.studentservice.infrastructure.adapter.input.rest.dto.StudentDtos.StudentUpdateRequest;
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


import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/students")
class StudentController {
    private final StudentUseCase useCase;
    private final AuditOutbox audit;

    StudentController(StudentUseCase useCase, AuditOutbox audit) {
        this.useCase = useCase;
        this.audit = audit;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    StudentResponse createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse res = toResponse(useCase.createStudent(toCommand(request)));
        audit.record("STUDENT_CREATED", "student-service", "STUDENT", res.id(), CurrentActor.required().userId(), Map.of("studentCode", res.studentCode(), "email", res.email()));
        return res;
    }

    @PutMapping("/{id}")
    StudentResponse updateStudent(@PathVariable UUID id, @Valid @RequestBody StudentUpdateRequest request) {
        return toResponse(useCase.updateStudent(id, new StudentCommand(null, request.studentCode(), request.firstName(), request.lastName(),
                request.gender(), request.dateOfBirth(), request.email(), request.phone(), request.address(), request.programId(), request.admissionDate())));
    }

    @GetMapping("/{id}")
    StudentResponse getStudent(@PathVariable UUID id) {
        return toResponse(useCase.getStudent(id));
    }

    @GetMapping
    PageResponse<StudentResponse> listStudents(
            @RequestParam(required = false) StudentStatus status,
            @RequestParam(required = false) UUID programId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return toPage(useCase.listStudents(status, programId, page, size), this::toResponse);
    }

    @PatchMapping("/{id}/activate")
    StudentResponse activateStudent(@PathVariable UUID id) {
        return toResponse(useCase.activateStudent(id));
    }

    @PatchMapping("/{id}/deactivate")
    StudentResponse deactivateStudent(@PathVariable UUID id) {
        return toResponse(useCase.deactivateStudent(id));
    }

    @PatchMapping("/{id}/suspend")
    StudentResponse suspendStudent(@PathVariable UUID id) {
        return toResponse(useCase.suspendStudent(id));
    }

    private StudentCommand toCommand(StudentRequest request) {
        return new StudentCommand(request.userId(), request.studentCode(), request.firstName(), request.lastName(), request.gender(), request.dateOfBirth(), request.email(), request.phone(), request.address(), request.programId(), request.admissionDate());
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(student.id(), student.userId(), student.studentCode(), student.firstName(), student.lastName(), student.gender(), student.dateOfBirth(), student.email(), student.phone(), student.address(), student.programId(), student.admissionDate(), student.status(), student.createdAt(), student.updatedAt());
    }

    private <T, R> PageResponse<R> toPage(PageResult<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.content().stream().map(mapper).toList(), page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
