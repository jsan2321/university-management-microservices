package com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest;

import com.panadi.ums.enrollmentservice.application.PageResult;
import com.panadi.ums.enrollmentservice.application.command.CreateEnrollmentCommand;
import com.panadi.ums.enrollmentservice.application.port.in.EnrollmentUseCase;
import com.panadi.ums.enrollmentservice.domain.model.Enrollment;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentDetail;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.CreateEnrollmentRequest;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.EnrollmentDetailResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.EnrollmentResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.PageResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.SectionStudentsResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/enrollments")
class EnrollmentController {
    private final EnrollmentUseCase useCase;
    private final EnrollmentActorClient students;

    EnrollmentController(EnrollmentUseCase useCase, EnrollmentActorClient students) {
        this.useCase = useCase;
        this.students = students;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    EnrollmentResponse createEnrollment(@Valid @RequestBody CreateEnrollmentRequest request) {
        return toResponse(useCase.createEnrollment(new CreateEnrollmentCommand(request.studentId(), request.semesterId(), request.sectionIds())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    EnrollmentResponse getEnrollment(@PathVariable UUID id) {
        return toResponse(useCase.getEnrollment(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    PageResponse<EnrollmentResponse> listEnrollments(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return toPage(useCase.listEnrollments(studentId, semesterId, status, page, size), this::toResponse);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    PageResponse<EnrollmentResponse> myEnrollments(
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) EnrollmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID studentId = students.byUser(CurrentActor.required().userId()).id();
        return toPage(useCase.listEnrollments(studentId, semesterId, status, page, size), this::toResponse);
    }

    @GetMapping("/sections/{sectionId}/students")
    @PreAuthorize("hasRole('ADMIN')")
    SectionStudentsResponse listActiveStudentsBySection(@PathVariable UUID sectionId) {
        return new SectionStudentsResponse(sectionId, useCase.listActiveStudentIdsBySection(sectionId));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    EnrollmentResponse cancelEnrollment(@PathVariable UUID id) {
        return toResponse(useCase.cancelEnrollment(id));
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(enrollment.id(), enrollment.studentId(), enrollment.semesterId(), enrollment.status(), enrollment.totalCredits(), enrollment.details().stream().map(this::toResponse).toList(), enrollment.createdAt(), enrollment.updatedAt(), enrollment.cancelledAt());
    }

    private EnrollmentDetailResponse toResponse(EnrollmentDetail detail) {
        return new EnrollmentDetailResponse(detail.id(), detail.sectionId(), detail.subjectId(), detail.credits());
    }

    private <T, R> PageResponse<R> toPage(PageResult<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.content().stream().map(mapper).toList(), page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
