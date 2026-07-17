package com.panadi.ums.enrollmentservice.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentDomainTests {
    @Test
    void rejectsEmptyDetails() {
        assertThatThrownBy(() -> Enrollment.create(UUID.randomUUID(), UUID.randomUUID(), List.of()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("section");
    }

    @Test
    void rejectsDuplicateSections() {
        UUID sectionId = UUID.randomUUID();
        List<EnrollmentDetail> details = List.of(
                EnrollmentDetail.create(sectionId, UUID.randomUUID(), 4),
                EnrollmentDetail.create(sectionId, UUID.randomUUID(), 3)
        );

        assertThatThrownBy(() -> Enrollment.create(UUID.randomUUID(), UUID.randomUUID(), details))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void rejectsDuplicateSubjects() {
        UUID subjectId = UUID.randomUUID();
        List<EnrollmentDetail> details = List.of(
                EnrollmentDetail.create(UUID.randomUUID(), subjectId, 4),
                EnrollmentDetail.create(UUID.randomUUID(), subjectId, 4)
        );

        assertThatThrownBy(() -> Enrollment.create(UUID.randomUUID(), UUID.randomUUID(), details))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("same subject");
    }

    @Test
    void cancelsActiveEnrollment() {
        Enrollment enrollment = Enrollment.create(UUID.randomUUID(), UUID.randomUUID(), List.of(EnrollmentDetail.create(UUID.randomUUID(), UUID.randomUUID(), 4)));

        Enrollment cancelled = enrollment.cancel();

        assertThat(cancelled.status()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(cancelled.cancelledAt()).isNotNull();
    }
}
