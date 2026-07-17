package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.student;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "student-service", path = "/api/v1/students")
interface StudentClient {
    @GetMapping("/{id}")
    StudentResponse getStudent(@PathVariable UUID id);

    record StudentResponse(UUID id, UUID programId, String status) {
    }
}
