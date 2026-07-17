package com.panadi.ums.academicservice.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcademicDomainTests {
    @Test
    void rejectsInvalidSubjectCredits() {
        assertThatThrownBy(() -> Subject.create(UUID.randomUUID(), "CS101", "Programming", null, 0, null, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("credits");
    }

    @Test
    void rejectsInvalidSectionCapacity() {
        assertThatThrownBy(() -> Section.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "CS101-A", 0, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    void rejectsInvalidSemesterDates() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        assertThatThrownBy(() -> Semester.create("2026-I", date, date))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("before");
    }

    @Test
    void rejectsInvalidScheduleTimes() {
        assertThatThrownBy(() -> new SectionSchedule(null, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(9, 0)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("before");
    }
}
