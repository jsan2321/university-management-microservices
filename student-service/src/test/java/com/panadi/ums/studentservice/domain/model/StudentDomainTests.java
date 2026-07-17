package com.panadi.ums.studentservice.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentDomainTests {
    @Test
    void rejectsStudentYoungerThanSeventeen() {
        assertThatThrownBy(() -> Student.create(null, "2026-0001", "Ada", "Lovelace", Gender.FEMALE, LocalDate.now().minusYears(16), "ada@example.com", null, null, UUID.randomUUID(), LocalDate.now()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("17");
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThatThrownBy(() -> Student.create(null, "", "Ada", "Lovelace", Gender.FEMALE, LocalDate.now().minusYears(20), "ada@example.com", null, null, UUID.randomUUID(), LocalDate.now()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("code");
    }

    @Test
    void supportsStatusTransitions() {
        Student student = Student.create(null, "2026-0001", "Ada", "Lovelace", Gender.FEMALE, LocalDate.now().minusYears(20), "ada@example.com", null, null, UUID.randomUUID(), LocalDate.now());

        assertThat(student.status()).isEqualTo(StudentStatus.ACTIVE);
        assertThat(student.deactivate().status()).isEqualTo(StudentStatus.INACTIVE);
        assertThat(student.suspend().status()).isEqualTo(StudentStatus.SUSPENDED);
        assertThat(student.deactivate().activate().status()).isEqualTo(StudentStatus.ACTIVE);
    }
}
