package com.panadi.ums.enrollmentservice.application.command;

import java.util.List;
import java.util.UUID;

public record CreateEnrollmentCommand(UUID studentId, UUID semesterId, List<UUID> sectionIds) {
}
