package com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest;

import com.panadi.ums.auditcommon.AuditOutbox;
import com.panadi.ums.enrollmentservice.application.PageResult;
import com.panadi.ums.enrollmentservice.application.command.CreateEnrollmentCommand;
import com.panadi.ums.enrollmentservice.application.port.in.EnrollmentUseCase;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort;
import com.panadi.ums.enrollmentservice.domain.model.Enrollment;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentDetail;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.CreateEnrollmentRequest;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.AddSectionRequest;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.EnrollmentDetailResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.EnrollmentResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.PageResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.SectionSummaryResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.SectionStudentsResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.SemesterSummaryResponse;
import com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest.dto.EnrollmentDtos.SubjectSummaryResponse;
import com.panadi.ums.security.CurrentActor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/enrollments")
class EnrollmentController {
    private final EnrollmentUseCase useCase;
    private final EnrollmentActorClient students;
    private final AcademicCatalogLookupPort academic;
    private final AuditOutbox audit;

    EnrollmentController(EnrollmentUseCase useCase, EnrollmentActorClient students, AcademicCatalogLookupPort academic, AuditOutbox audit) {
        this.useCase = useCase;
        this.students = students;
        this.academic = academic;
        this.audit = audit;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    EnrollmentResponse createEnrollment(@Valid @RequestBody CreateEnrollmentRequest request) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("STUDENT")) {
            UUID studentId = students.byUser(actor.userId()).id();
            if (!request.studentId().equals(studentId)) {
                throw new AccessDeniedException("You do not have permission to enroll another student");
            }
            var semester = academic.getSemester(request.semesterId());
            if (!semester.isRegistrationOpen()) {
                throw new AccessDeniedException("Registration is not currently open for this semester");
            }
        }

        Enrollment enrollment = useCase.createEnrollment(new CreateEnrollmentCommand(request.studentId(), request.semesterId(), request.sectionIds()));
        audit.record("EnrollmentCreated", "enrollment-service", "Enrollment", enrollment.id(), null,
                Map.of("studentId", enrollment.studentId(), "semesterId", enrollment.semesterId(), "status", enrollment.status().name()));
        return toResponse(enrollment);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    EnrollmentResponse getEnrollment(@PathVariable UUID id) {
        Enrollment enrollment = useCase.getEnrollment(id);
        verifyOwnership(enrollment);
        return toResponse(enrollment);
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
    @Transactional
    EnrollmentResponse cancelEnrollment(@PathVariable UUID id) {
        Enrollment enrollment = useCase.cancelEnrollment(id);
        audit.record("EnrollmentCancelled", "enrollment-service", "Enrollment", enrollment.id(), null,
                Map.of("studentId", enrollment.studentId(), "semesterId", enrollment.semesterId(), "status", enrollment.status().name()));
        return toResponse(enrollment);
    }

    @PostMapping("/{id}/sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    EnrollmentResponse addSection(@PathVariable UUID id, @Valid @RequestBody AddSectionRequest request) {
        Enrollment enrollment = useCase.getEnrollment(id);
        verifyOwnership(enrollment);
        verifyAddDrop(enrollment);
        return toResponse(useCase.addSection(id, request.sectionId()));
    }

    @PatchMapping("/{id}/sections/{sectionId}/drop")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    EnrollmentResponse dropSection(@PathVariable UUID id, @PathVariable UUID sectionId) {
        Enrollment enrollment = useCase.getEnrollment(id);
        verifyOwnership(enrollment);
        verifyAddDrop(enrollment);
        return toResponse(useCase.dropSection(id, sectionId));
    }

    // toggleAddDrop removed

    private void verifyOwnership(Enrollment enrollment) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("STUDENT")) {
            UUID studentId = students.byUser(actor.userId()).id();
            if (!enrollment.studentId().equals(studentId)) {
                throw new AccessDeniedException("You do not have permission to access this enrollment");
            }
        }
    }

    private void verifyAddDrop(Enrollment enrollment) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("STUDENT")) {
            var semester = academic.getSemester(enrollment.semesterId());
            if (!semester.isRegistrationOpen()) {
                throw new AccessDeniedException("Registration is not currently open for this semester");
            }
        }
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        var semester = academic.getSemester(enrollment.semesterId());
        return new EnrollmentResponse(enrollment.id(), enrollment.studentId(), enrollment.semesterId(), new SemesterSummaryResponse(semester.id(), semester.name()), enrollment.status(), enrollment.totalCredits(), enrollment.details().stream().map(this::toResponse).toList(), semester.isRegistrationOpen(), enrollment.createdAt(), enrollment.updatedAt(), enrollment.cancelledAt());
    }

    private EnrollmentDetailResponse toResponse(EnrollmentDetail detail) {
        var section = academic.getSection(detail.sectionId());
        var subject = academic.getSubject(detail.subjectId());
        return new EnrollmentDetailResponse(
                detail.id(), detail.sectionId(), detail.subjectId(), detail.credits(),
                new SectionSummaryResponse(section.id(), section.sectionCode()),
                new SubjectSummaryResponse(subject.id(), subject.code(), subject.name())
        );
    }

    private <T, R> PageResponse<R> toPage(PageResult<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.content().stream().map(mapper).toList(), page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
