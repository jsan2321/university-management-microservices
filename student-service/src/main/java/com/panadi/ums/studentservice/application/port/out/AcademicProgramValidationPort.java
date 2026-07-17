package com.panadi.ums.studentservice.application.port.out;

import java.util.UUID;

public interface AcademicProgramValidationPort {
    void validateActiveProgram(UUID programId);
}
