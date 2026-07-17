package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.enrollmentservice.application.PageResult;
import com.panadi.ums.enrollmentservice.application.port.out.EnrollmentRepositoryPort;
import com.panadi.ums.enrollmentservice.domain.model.Enrollment;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentDetail;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
class EnrollmentPersistenceAdapter implements EnrollmentRepositoryPort {
    private final EnrollmentJpaRepository enrollments;
    private final EnrollmentDetailJpaRepository details;

    EnrollmentPersistenceAdapter(EnrollmentJpaRepository enrollments, EnrollmentDetailJpaRepository details) {
        this.enrollments = enrollments;
        this.details = details;
    }

    @Override
    public Enrollment saveEnrollment(Enrollment enrollment) {
        return toDomain(enrollments.save(toEntity(enrollment)));
    }

    @Override
    public Optional<Enrollment> findEnrollmentById(UUID id) {
        return enrollments.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<Enrollment> findEnrollments(UUID studentId, UUID semesterId, EnrollmentStatus status, int page, int size) {
        return toPage(enrollments.findAll(enrollmentSpec(studentId, semesterId, status), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsActiveEnrollment(UUID studentId, UUID semesterId) {
        return enrollments.existsByStudentIdAndSemesterIdAndStatus(studentId, semesterId, EnrollmentStatus.ACTIVE);
    }

    @Override
    public long countActiveEnrollmentDetailsBySectionId(UUID sectionId) {
        return details.countBySectionIdAndEnrollmentStatus(sectionId, EnrollmentStatus.ACTIVE);
    }

    @Override
    public List<UUID> findActiveStudentIdsBySectionId(UUID sectionId) {
        return details.findStudentIdsBySectionIdAndEnrollmentStatus(sectionId, EnrollmentStatus.ACTIVE);
    }

    private EnrollmentEntity toEntity(Enrollment enrollment) {
        EnrollmentEntity entity = new EnrollmentEntity();
        entity.id = enrollment.id() == null ? UUID.randomUUID() : enrollment.id();
        entity.studentId = enrollment.studentId();
        entity.semesterId = enrollment.semesterId();
        entity.status = enrollment.status();
        entity.createdAt = enrollment.createdAt() == null ? LocalDateTime.now() : enrollment.createdAt();
        entity.updatedAt = enrollment.updatedAt() == null ? LocalDateTime.now() : enrollment.updatedAt();
        entity.cancelledAt = enrollment.cancelledAt();
        enrollment.details().forEach(detail -> {
            EnrollmentDetailEntity detailEntity = new EnrollmentDetailEntity();
            detailEntity.id = detail.id() == null ? UUID.randomUUID() : detail.id();
            detailEntity.enrollment = entity;
            detailEntity.sectionId = detail.sectionId();
            detailEntity.subjectId = detail.subjectId();
            detailEntity.credits = detail.credits();
            detailEntity.createdAt = detail.createdAt() == null ? LocalDateTime.now() : detail.createdAt();
            entity.details.add(detailEntity);
        });
        return entity;
    }

    private Enrollment toDomain(EnrollmentEntity entity) {
        List<EnrollmentDetail> enrollmentDetails = entity.details.stream()
                .map(detail -> new EnrollmentDetail(detail.id, detail.sectionId, detail.subjectId, detail.credits, detail.createdAt))
                .toList();
        return new Enrollment(entity.id, entity.studentId, entity.semesterId, entity.status, enrollmentDetails, entity.createdAt, entity.updatedAt, entity.cancelledAt);
    }

    private Specification<EnrollmentEntity> enrollmentSpec(UUID studentId, UUID semesterId, EnrollmentStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (studentId != null) predicates.add(cb.equal(root.get("studentId"), studentId));
            if (semesterId != null) predicates.add(cb.equal(root.get("semesterId"), semesterId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private <T, R> PageResult<R> toPage(Page<T> page, Function<T, R> mapper) {
        return new PageResult<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
