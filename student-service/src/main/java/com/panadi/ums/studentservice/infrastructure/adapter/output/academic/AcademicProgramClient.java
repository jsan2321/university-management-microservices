package com.panadi.ums.studentservice.infrastructure.adapter.output.academic;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "academic-service", path = "/internal/academic")
interface AcademicProgramClient {
    @GetMapping("/programs/{id}")
    AcademicProgramResponse getProgram(@PathVariable UUID id);

    record AcademicProgramResponse(UUID id, String code, String name, String status) {
    }
}
