package com.panadi.ums.academicservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.SemesterStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class PersistenceEntities {
    private PersistenceEntities() {
    }

    @Entity
    @Table(name = "departments")
    static class DepartmentEntity {
        @Id UUID id;
        String code;
        String name;
        String description;
        @Enumerated(EnumType.STRING) AcademicStatus status;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Entity
    @Table(name = "programs")
    static class ProgramEntity {
        @Id UUID id;
        UUID departmentId;
        String code;
        String name;
        int durationSemesters;
        int totalCredits;
        @Enumerated(EnumType.STRING) AcademicStatus status;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Entity
    @Table(name = "teachers")
    static class TeacherEntity {
        @Id UUID id;
        UUID departmentId;
        UUID userId;
        String teacherCode;
        String firstName;
        String lastName;
        String email;
        String phone;
        LocalDate hireDate;
        @Enumerated(EnumType.STRING) AcademicStatus status;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Entity
    @Table(name = "semesters")
    static class SemesterEntity {
        @Id UUID id;
        String name;
        LocalDate startDate;
        LocalDate endDate;
        @Enumerated(EnumType.STRING) SemesterStatus status;
        boolean isRegistrationOpen;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Entity
    @Table(name = "subjects")
    static class SubjectEntity {
        @Id UUID id;
        UUID programId;
        String code;
        String name;
        String description;
        int credits;
        Integer minimumCreditsRequired;
        @Enumerated(EnumType.STRING) AcademicStatus status;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }

    @Entity
    @Table(name = "subject_prerequisites")
    static class SubjectPrerequisiteEntity {
        @Id UUID id;
        UUID subjectId;
        UUID prerequisiteSubjectId;
        LocalDateTime createdAt;
    }

    @Entity
    @Table(name = "sections")
    static class SectionEntity {
        @Id UUID id;
        UUID subjectId;
        UUID teacherId;
        UUID semesterId;
        String sectionCode;
        int capacity;
        @Enumerated(EnumType.STRING) AcademicStatus status;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
        List<SectionScheduleEntity> schedules = new ArrayList<>();
    }

    @Entity
    @Table(name = "section_schedules")
    static class SectionScheduleEntity {
        @Id UUID id;
        @jakarta.persistence.ManyToOne(optional = false)
        @jakarta.persistence.JoinColumn(name = "section_id")
        SectionEntity section;
        @Enumerated(EnumType.STRING) DayOfWeek dayOfWeek;
        LocalTime startTime;
        LocalTime endTime;
        LocalDateTime createdAt;
    }
}
