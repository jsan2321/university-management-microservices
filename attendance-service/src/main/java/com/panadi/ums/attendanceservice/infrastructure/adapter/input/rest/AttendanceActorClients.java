package com.panadi.ums.attendanceservice.infrastructure.adapter.input.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "academic-service", contextId = "attendanceActorTeacherClient", path = "/internal/academic")
interface AttendanceActorTeacherClient {
    @GetMapping("/teachers/by-user/{userId}") TeacherProfile byUser(@PathVariable UUID userId);
    record TeacherProfile(UUID id, UUID userId) {}
}

@FeignClient(name = "student-service", contextId = "attendanceActorStudentClient", path = "/internal/students")
interface AttendanceActorStudentClient {
    @GetMapping("/{studentId}") StudentProfile byId(@PathVariable UUID studentId);
    @GetMapping("/by-user/{userId}") StudentProfile byUser(@PathVariable UUID userId);
    record StudentProfile(UUID id, UUID userId, String studentCode, String firstName, String lastName) {}
}
