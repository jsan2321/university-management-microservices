package com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest;

import com.panadi.ums.auditcommon.AuditOutbox;
import com.panadi.ums.assignmentservice.application.PageResult;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.CreateAssignment;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.GradeSubmission;
import com.panadi.ums.assignmentservice.application.command.AssignmentCommands.SubmitAssignment;
import com.panadi.ums.assignmentservice.application.port.in.AssignmentUseCase;
import com.panadi.ums.assignmentservice.domain.model.Assignment;
import com.panadi.ums.assignmentservice.domain.model.AssignmentStatus;
import com.panadi.ums.assignmentservice.domain.model.Submission;
import com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto.AssignmentDtos.AssignmentResponse;
import com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto.AssignmentDtos.CreateAssignmentRequest;
import com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto.AssignmentDtos.GradeSubmissionRequest;
import com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto.AssignmentDtos.GradedSubmissionResponse;
import com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto.AssignmentDtos.PageResponse;
import com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto.AssignmentDtos.SubmissionResponse;
import com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto.AssignmentDtos.SubmitAssignmentRequest;
import com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest.dto.AssignmentDtos.TeacherActionRequest;
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
@RequestMapping("/api/v1/assignments")
class AssignmentController {
    private final AssignmentUseCase useCase;
    private final ActorTeacherClient teachers;
    private final ActorStudentClient students;
    private final ActorRosterClient rosters;
    private final AuditOutbox audit;

    AssignmentController(AssignmentUseCase useCase, ActorTeacherClient teachers, ActorStudentClient students, ActorRosterClient rosters, AuditOutbox audit) {
        this.useCase = useCase;
        this.teachers = teachers;
        this.students = students;
        this.rosters = rosters;
        this.audit = audit;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    AssignmentResponse create(@Valid @RequestBody CreateAssignmentRequest request) {
        return toResponse(useCase.createAssignment(new CreateAssignment(request.sectionId(), actingTeacher(request.teacherId()), request.title(), request.description(), request.dueAt(), request.maxPoints())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    AssignmentResponse get(@PathVariable UUID id) {
        Assignment value = useCase.getAssignment(id);
        requireAssignmentAccess(value);
        requirePublishedForStudent(value);
        return toResponse(value);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    PageResponse<AssignmentResponse> list(@RequestParam(required = false) UUID sectionId, @RequestParam(required = false) AssignmentStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        requireSectionAccess(sectionId);
        if (CurrentActor.required().hasRole("STUDENT")) status = AssignmentStatus.PUBLISHED;
        return toPage(useCase.listAssignments(sectionId, status, page, size), this::toResponse);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    PageResponse<AssignmentResponse> listMine(@RequestParam UUID sectionId, @RequestParam(required = false) AssignmentStatus status,
                                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        requireSectionAccess(sectionId);
        if (CurrentActor.required().hasRole("STUDENT")) status = AssignmentStatus.PUBLISHED;
        return toPage(useCase.listAssignments(sectionId, status, page, size), this::toResponse);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    AssignmentResponse publish(@PathVariable UUID id, @Valid @RequestBody TeacherActionRequest request) {
        return toResponse(useCase.publishAssignment(id, actingTeacher(request.teacherId())));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    AssignmentResponse close(@PathVariable UUID id, @Valid @RequestBody TeacherActionRequest request) {
        return toResponse(useCase.closeAssignment(id, actingTeacher(request.teacherId())));
    }

    @PostMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    SubmissionResponse submit(@PathVariable UUID assignmentId, @Valid @RequestBody SubmitAssignmentRequest request) {
        return toPublicResponse(useCase.submit(assignmentId, new SubmitAssignment(actingStudent(request.studentId()), request.content())));
    }

    @PostMapping("/{assignmentId}/submissions/me")
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    SubmissionResponse submitMine(@PathVariable UUID assignmentId, @Valid @RequestBody SubmitAssignmentRequest request) {
        return toPublicResponse(useCase.submit(assignmentId, new SubmitAssignment(actingStudent(null), request.content())));
    }

    @GetMapping("/submissions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    SubmissionResponse getSubmission(@PathVariable UUID id) {
        Submission value = useCase.getSubmission(id);
        requireSubmissionAccess(value);
        return toPublicResponse(value);
    }

    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    PageResponse<SubmissionResponse> listSubmissions(@PathVariable UUID assignmentId, @RequestParam(required = false) UUID studentId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Assignment assignment = useCase.getAssignment(assignmentId);
        requireAssignmentAccess(assignment);
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("STUDENT")) studentId = actingStudent(null);
        return toPage(useCase.listSubmissions(assignmentId, studentId, page, size), this::toPublicResponse);
    }

    @GetMapping("/{assignmentId}/submissions/me")
    @PreAuthorize("hasRole('STUDENT')")
    PageResponse<SubmissionResponse> listMySubmissions(@PathVariable UUID assignmentId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        Assignment assignment = useCase.getAssignment(assignmentId);
        requireAssignmentAccess(assignment);
        return toPage(useCase.listSubmissions(assignmentId, actingStudent(null), page, size), this::toPublicResponse);
    }

    @PatchMapping("/submissions/{id}/grade")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    GradedSubmissionResponse grade(@PathVariable UUID id, @Valid @RequestBody GradeSubmissionRequest request) {
        return toGradedResponse(useCase.gradeSubmission(id, new GradeSubmission(actingTeacher(request.teacherId()), request.score(), request.feedback())));
    }

    @PatchMapping("/submissions/{id}/release-grade")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Transactional
    SubmissionResponse releaseGrade(@PathVariable UUID id, @Valid @RequestBody TeacherActionRequest request) {
        UUID teacherId = actingTeacher(request.teacherId());
        Submission submission = useCase.releaseGrade(id, teacherId);
        audit.record("GradeReleased", "assignment-service", "Submission", submission.id(), teacherId,
                Map.of("assignmentId", submission.assignmentId(), "studentId", submission.studentId()));
        return toPublicResponse(submission);
    }

    private UUID actingTeacher(UUID adminSuppliedId) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("TEACHER")) return teachers.byUser(actor.userId()).id();
        if (actor.hasRole("ADMIN") && adminSuppliedId != null) return adminSuppliedId;
        throw new AccessDeniedException("A teacher identity is required");
    }

    private UUID actingStudent(UUID adminSuppliedId) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("STUDENT")) return students.byUser(actor.userId()).id();
        if (actor.hasRole("ADMIN") && adminSuppliedId != null) return adminSuppliedId;
        throw new AccessDeniedException("A student identity is required");
    }

    private void requireSectionAccess(UUID sectionId) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("ADMIN")) return;
        if (sectionId == null) throw new AccessDeniedException("A section filter is required");
        if (actor.hasRole("TEACHER")) {
            UUID teacherId = teachers.byUser(actor.userId()).id();
            if (!teacherId.equals(teachers.section(sectionId).teacherId())) throw new AccessDeniedException("Section is not assigned to this teacher");
            return;
        }
        if (actor.hasRole("STUDENT")) {
            UUID studentId = students.byUser(actor.userId()).id();
            if (!rosters.roster(sectionId).studentIds().contains(studentId)) throw new AccessDeniedException("Student is not enrolled in this section");
            return;
        }
        throw new AccessDeniedException("Assignment access is not allowed");
    }

    private void requireAssignmentAccess(Assignment assignment) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("ADMIN")) return;
        if (actor.hasRole("TEACHER") && assignment.teacherId().equals(teachers.byUser(actor.userId()).id())) return;
        if (actor.hasRole("STUDENT") && rosters.roster(assignment.sectionId()).studentIds().contains(students.byUser(actor.userId()).id())) return;
        throw new AccessDeniedException("Assignment does not belong to the authenticated user");
    }

    private void requirePublishedForStudent(Assignment assignment) {
        if (CurrentActor.required().hasRole("STUDENT") && assignment.status() != AssignmentStatus.PUBLISHED) {
            throw new AccessDeniedException("Assignment is not published");
        }
    }

    private void requireSubmissionAccess(Submission submission) {
        CurrentActor actor = CurrentActor.required();
        if (actor.hasRole("ADMIN")) return;
        if (actor.hasRole("STUDENT") && submission.studentId().equals(students.byUser(actor.userId()).id())) return;
        Assignment assignment = useCase.getAssignment(submission.assignmentId());
        if (actor.hasRole("TEACHER") && assignment.teacherId().equals(teachers.byUser(actor.userId()).id())) return;
        throw new AccessDeniedException("Submission does not belong to the authenticated user");
    }

    private AssignmentResponse toResponse(Assignment value) {
        return new AssignmentResponse(value.id(), value.sectionId(), value.teacherId(), value.title(), value.description(), value.dueAt(), value.maxPoints(), value.status(), value.createdAt(), value.updatedAt(), value.publishedAt(), value.closedAt());
    }

    private SubmissionResponse toPublicResponse(Submission value) {
        boolean released = value.gradeReleased();
        return new SubmissionResponse(value.id(), value.assignmentId(), value.studentId(), value.content(), value.status(), released ? value.score() : null, released ? value.feedback() : null, released, value.submittedAt(), released ? value.gradedAt() : null, value.gradeReleasedAt(), value.updatedAt());
    }

    private GradedSubmissionResponse toGradedResponse(Submission value) {
        return new GradedSubmissionResponse(value.id(), value.assignmentId(), value.studentId(), value.content(), value.status(), value.score(), value.feedback(), value.gradeReleased(), value.submittedAt(), value.gradedAt(), value.gradeReleasedAt(), value.updatedAt());
    }

    private <T, R> PageResponse<R> toPage(PageResult<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.content().stream().map(mapper).toList(), page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
