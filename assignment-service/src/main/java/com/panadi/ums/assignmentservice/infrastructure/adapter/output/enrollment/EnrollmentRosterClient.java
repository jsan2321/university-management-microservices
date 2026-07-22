package com.panadi.ums.assignmentservice.infrastructure.adapter.output.enrollment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "enrollment-service", path = "/internal/enrollments")
interface EnrollmentRosterClient {
    @GetMapping("/sections/{sectionId}/students")
    SectionStudentsResponse getActiveStudents(@PathVariable UUID sectionId);

    record SectionStudentsResponse(UUID sectionId, List<UUID> studentIds) { }
}
