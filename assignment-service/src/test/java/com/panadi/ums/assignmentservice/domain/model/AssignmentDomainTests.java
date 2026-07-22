package com.panadi.ums.assignmentservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssignmentDomainTests {
    @Test
    void rejectsDueDateBeforeCreation() {
        assertThatThrownBy(() -> Assignment.create(UUID.randomUUID(), UUID.randomUUID(), "Project", null, LocalDateTime.now().minusMinutes(1), BigDecimal.TEN))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Due date");
    }

    @Test
    void assignmentMovesFromDraftToPublishedAndClosed() {
        Assignment assignment = assignment().publish().close();

        assertThat(assignment.status()).isEqualTo(AssignmentStatus.CLOSED);
        assertThat(assignment.publishedAt()).isNotNull();
        assertThat(assignment.closedAt()).isNotNull();
    }

    @Test
    void submissionIsMarkedLateAfterDueDate() {
        Submission submission = Submission.create(UUID.randomUUID(), UUID.randomUUID(), "solution", LocalDateTime.now().minusSeconds(1));

        assertThat(submission.status()).isEqualTo(SubmissionStatus.LATE);
    }

    @Test
    void gradeCannotExceedMaximumPoints() {
        Submission submission = Submission.create(UUID.randomUUID(), UUID.randomUUID(), "solution", LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> submission.grade(BigDecimal.valueOf(101), null, BigDecimal.valueOf(100)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("maximum");
    }

    @Test
    void gradeMustExistBeforeRelease() {
        Submission submission = Submission.create(UUID.randomUUID(), UUID.randomUUID(), "solution", LocalDateTime.now().plusDays(1));

        assertThatThrownBy(submission::releaseGrade)
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("graded");
    }

    private Assignment assignment() {
        return Assignment.create(UUID.randomUUID(), UUID.randomUUID(), "Project", "Build it", LocalDateTime.now().plusDays(7), BigDecimal.valueOf(100));
    }
}
