package com.panadi.ums.assignmentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.assignmentservice.application.PageResult;
import com.panadi.ums.assignmentservice.application.port.out.AssignmentRepositoryPort;
import com.panadi.ums.assignmentservice.domain.model.Assignment;
import com.panadi.ums.assignmentservice.domain.model.AssignmentStatus;
import com.panadi.ums.assignmentservice.domain.model.Submission;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Repository
@Transactional
class AssignmentPersistenceAdapter implements AssignmentRepositoryPort {
    private final AssignmentJpaRepository assignments;
    private final SubmissionJpaRepository submissions;

    AssignmentPersistenceAdapter(AssignmentJpaRepository assignments, SubmissionJpaRepository submissions) {
        this.assignments = assignments;
        this.submissions = submissions;
    }

    @Override
    public Assignment saveAssignment(Assignment assignment) {
        return toDomain(assignments.save(toEntity(assignment)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Assignment> findAssignmentById(UUID id) {
        return assignments.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Assignment> findAssignments(UUID sectionId, AssignmentStatus status, int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toPage(assignments.findAll(assignmentSpec(sectionId, status), request), this::toDomain);
    }

    @Override
    public Submission saveSubmission(Submission submission) {
        return toDomain(submissions.save(toEntity(submission)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Submission> findSubmissionById(UUID id) {
        return submissions.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsSubmission(UUID assignmentId, UUID studentId) {
        return submissions.existsByAssignmentIdAndStudentId(assignmentId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Submission> findSubmissions(UUID assignmentId, UUID studentId, int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        return toPage(submissions.findAll(submissionSpec(assignmentId, studentId), request), this::toDomain);
    }

    private AssignmentEntity toEntity(Assignment domain) {
        AssignmentEntity entity = new AssignmentEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.sectionId = domain.sectionId();
        entity.teacherId = domain.teacherId();
        entity.title = domain.title();
        entity.description = domain.description();
        entity.dueAt = domain.dueAt();
        entity.maxPoints = domain.maxPoints();
        entity.status = domain.status();
        entity.createdAt = domain.createdAt() == null ? LocalDateTime.now() : domain.createdAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        entity.publishedAt = domain.publishedAt();
        entity.closedAt = domain.closedAt();
        return entity;
    }

    private Assignment toDomain(AssignmentEntity entity) {
        return new Assignment(entity.id, entity.sectionId, entity.teacherId, entity.title, entity.description, entity.dueAt, entity.maxPoints, entity.status, entity.createdAt, entity.updatedAt, entity.publishedAt, entity.closedAt);
    }

    private SubmissionEntity toEntity(Submission domain) {
        SubmissionEntity entity = new SubmissionEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.assignmentId = domain.assignmentId();
        entity.studentId = domain.studentId();
        entity.content = domain.content();
        entity.status = domain.status();
        entity.score = domain.score();
        entity.feedback = domain.feedback();
        entity.submittedAt = domain.submittedAt() == null ? LocalDateTime.now() : domain.submittedAt();
        entity.gradedAt = domain.gradedAt();
        entity.gradeReleasedAt = domain.gradeReleasedAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        return entity;
    }

    private Submission toDomain(SubmissionEntity entity) {
        return new Submission(entity.id, entity.assignmentId, entity.studentId, entity.content, entity.status, entity.score, entity.feedback, entity.submittedAt, entity.gradedAt, entity.gradeReleasedAt, entity.updatedAt);
    }

    private Specification<AssignmentEntity> assignmentSpec(UUID sectionId, AssignmentStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (sectionId != null) predicates.add(cb.equal(root.get("sectionId"), sectionId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<SubmissionEntity> submissionSpec(UUID assignmentId, UUID studentId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("assignmentId"), assignmentId));
            if (studentId != null) predicates.add(cb.equal(root.get("studentId"), studentId));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private <T, R> PageResult<R> toPage(Page<T> page, Function<T, R> mapper) {
        return new PageResult<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
