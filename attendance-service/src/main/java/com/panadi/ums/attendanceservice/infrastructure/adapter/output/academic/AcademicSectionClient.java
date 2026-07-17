package com.panadi.ums.attendanceservice.infrastructure.adapter.output.academic;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "academic-service", path = "/api/v1/academic")
interface AcademicSectionClient {
    @GetMapping("/sections/{id}")
    SectionResponse getSection(@PathVariable UUID id);

    record SectionResponse(UUID id, UUID teacherId, String status) {
    }
}
