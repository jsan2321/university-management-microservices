package com.panadi.ums.identityservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.UUID;

@FeignClient(name = "academic-service", contextId = "teacherProvisioningClient", path = "/internal/teachers")
interface TeacherProfileClient {
    @PostMapping
    ProfileResponse create(@RequestBody TeacherProfileRequest request);
    @PatchMapping("/{profileId}/identity/{userId}")
    ProfileResponse link(@PathVariable UUID profileId, @PathVariable UUID userId);
}

@FeignClient(name = "student-service", contextId = "studentProvisioningClient", path = "/internal/students")
interface StudentProfileClient {
    @PostMapping
    ProfileResponse create(@RequestBody StudentProfileRequest request);
    @PatchMapping("/{profileId}/identity/{userId}")
    ProfileResponse link(@PathVariable UUID profileId, @PathVariable UUID userId);
}

record TeacherProfileRequest(UUID departmentId, UUID userId, String teacherCode, String firstName,
                             String lastName, String email, String phone, LocalDate hireDate) {}
record StudentProfileRequest(UUID userId, String studentCode, String firstName, String lastName, String gender,
                             LocalDate dateOfBirth, String email, String phone, String address, UUID programId,
                             LocalDate admissionDate) {}
record ProfileResponse(UUID id, UUID userId) {}
