package com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest;

import com.panadi.ums.enrollmentservice.application.port.in.EnrollmentUseCase;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.SectionStudentsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/enrollments")
class InternalEnrollmentController {
    private final EnrollmentUseCase useCase;

    InternalEnrollmentController(EnrollmentUseCase useCase) { this.useCase = useCase; }

    @GetMapping("/sections/{sectionId}/students")
    SectionStudentsResponse roster(@PathVariable UUID sectionId) {
        return new SectionStudentsResponse(sectionId, useCase.listActiveStudentIdsBySection(sectionId));
    }
}
