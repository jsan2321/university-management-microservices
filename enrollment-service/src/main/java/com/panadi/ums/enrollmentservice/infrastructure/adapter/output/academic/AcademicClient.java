package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.academic;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@FeignClient(name = "academic-service", path = "/internal/academic")
interface AcademicClient {
    @GetMapping("/semesters/{id}")
    SemesterResponse getSemester(@PathVariable UUID id);

    @GetMapping("/sections/{id}")
    SectionResponse getSection(@PathVariable UUID id);

    @GetMapping("/subjects/{id}")
    SubjectResponse getSubject(@PathVariable UUID id);

    record SemesterResponse(UUID id, String name, String status, boolean isRegistrationOpen) {
    }

    record SectionResponse(UUID id, UUID subjectId, UUID teacherId, UUID semesterId, String sectionCode, int capacity, List<ScheduleResponse> schedules, String status) {
    }

    record ScheduleResponse(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    }

    record SubjectResponse(UUID id, UUID programId, String code, String name, int credits, Set<UUID> prerequisiteSubjectIds, String status) {
    }
}
