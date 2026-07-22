package com.panadi.ums.assignmentservice.infrastructure.adapter.input.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "academic-service", contextId = "assignmentActorTeacherClient", path = "/internal/academic")
interface ActorTeacherClient {
    @GetMapping("/teachers/by-user/{userId}") TeacherProfile byUser(@PathVariable UUID userId);
    @GetMapping("/sections/{sectionId}") SectionProfile section(@PathVariable UUID sectionId);
    record TeacherProfile(UUID id, UUID userId) {}
    record SectionProfile(UUID id, UUID teacherId, String status) {}
}

@FeignClient(name = "student-service", contextId = "assignmentActorStudentClient", path = "/internal/students")
interface ActorStudentClient {
    @GetMapping("/by-user/{userId}") StudentProfile byUser(@PathVariable UUID userId);
    record StudentProfile(UUID id, UUID userId) {}
}

@FeignClient(name = "enrollment-service", contextId = "assignmentActorRosterClient", path = "/internal/enrollments")
interface ActorRosterClient {
    @GetMapping("/sections/{sectionId}/students") SectionStudents roster(@PathVariable UUID sectionId);
    record SectionStudents(UUID sectionId, List<UUID> studentIds) {}
}
