package com.panadi.ums.attendanceservice.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttendanceDomainTests {
    @Test
    void rejectsInvalidSessionNumber() {
        assertThatThrownBy(() -> AttendanceSession.create(UUID.randomUUID(), 0, LocalDate.now(), "Intro"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void updatesAttendanceStatus() {
        Attendance attendance = Attendance.create(UUID.randomUUID(), UUID.randomUUID(), AttendanceStatus.ABSENT);

        assertThat(attendance.updateStatus(AttendanceStatus.PRESENT).status()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void percentageIsZeroWhenThereAreNoSessions() {
        AttendancePercentage percentage = AttendancePercentage.calculate(UUID.randomUUID(), UUID.randomUUID(), 0, 0);

        assertThat(percentage.percentage()).isZero();
        assertThat(percentage.eligibleForFinalEvaluation()).isFalse();
    }

    @Test
    void percentageEligibilityUsesSeventyPercentThreshold() {
        AttendancePercentage percentage = AttendancePercentage.calculate(UUID.randomUUID(), UUID.randomUUID(), 7, 10);

        assertThat(percentage.percentage()).isEqualTo(70.0);
        assertThat(percentage.eligibleForFinalEvaluation()).isTrue();
    }
}
