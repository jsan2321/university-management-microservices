package com.panadi.ums.enrollmentservice.infrastructure.adapter.input.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "student-service", contextId = "enrollmentActorStudentClient", path = "/internal/students")
interface EnrollmentActorClient {
    @GetMapping("/by-user/{userId}") StudentProfile byUser(@PathVariable UUID userId);
    record StudentProfile(UUID id, UUID userId) {}
}
