package com.panadi.ums.enrollmentservice.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record Enrollment(
        UUID id,
        UUID studentId,
        UUID semesterId,
        EnrollmentStatus status,
        List<EnrollmentDetail> details,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime cancelledAt
) {
    public Enrollment {
        EnrollmentDetail.requireId(studentId, "Student id is required");
        EnrollmentDetail.requireId(semesterId, "Semester id is required");
        if (status == null) {
            throw new DomainValidationException("Enrollment status is required");
        }
        if (details == null || details.isEmpty()) {
            throw new DomainValidationException("Enrollment must contain at least one section");
        }
        details = List.copyOf(details);
        ensureNoDuplicateSections(details);
        ensureNoDuplicateSubjects(details);
    }

    public static Enrollment create(UUID studentId, UUID semesterId, List<EnrollmentDetail> details) {
        LocalDateTime now = LocalDateTime.now();
        return new Enrollment(null, studentId, semesterId, EnrollmentStatus.ACTIVE, details, now, now, null);
    }

    public Enrollment cancel() {
        if (status != EnrollmentStatus.ACTIVE) {
            throw new DomainValidationException("Only active enrollments can be cancelled");
        }
        LocalDateTime now = LocalDateTime.now();
        return new Enrollment(id, studentId, semesterId, EnrollmentStatus.CANCELLED, details, createdAt, now, now);
    }

    public Enrollment add(EnrollmentDetail detail) {
        if (status != EnrollmentStatus.ACTIVE) throw new DomainValidationException("Only active enrollments can be changed");
        List<EnrollmentDetail> updated = new java.util.ArrayList<>(details);
        updated.add(detail);
        return new Enrollment(id, studentId, semesterId, status, updated, createdAt, LocalDateTime.now(), cancelledAt);
    }

    public Enrollment drop(UUID sectionId) {
        if (status != EnrollmentStatus.ACTIVE) throw new DomainValidationException("Only active enrollments can be changed");
        List<EnrollmentDetail> updated = details.stream().filter(detail -> !detail.sectionId().equals(sectionId)).toList();
        if (updated.size() == details.size()) throw new DomainValidationException("Section is not part of this enrollment");
        if (updated.isEmpty()) throw new DomainValidationException("Use cancellation to remove the final section");
        return new Enrollment(id, studentId, semesterId, status, updated, createdAt, LocalDateTime.now(), cancelledAt);
    }

    public int totalCredits() {
        return details.stream().mapToInt(EnrollmentDetail::credits).sum();
    }

    private static void ensureNoDuplicateSections(List<EnrollmentDetail> details) {
        Set<UUID> sectionIds = new HashSet<>();
        for (EnrollmentDetail detail : details) {
            if (!sectionIds.add(detail.sectionId())) {
                throw new DomainValidationException("Duplicate sections are not allowed");
            }
        }
    }

    private static void ensureNoDuplicateSubjects(List<EnrollmentDetail> details) {
        Set<UUID> subjectIds = new HashSet<>();
        for (EnrollmentDetail detail : details) {
            if (!subjectIds.add(detail.subjectId())) {
                throw new DomainValidationException("Student cannot enroll in multiple sections of the same subject");
            }
        }
    }
}
