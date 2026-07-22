package com.panadi.ums.identityservice;

import com.panadi.ums.identityservice.ProvisioningDtos.ErrorResponse;
import com.panadi.ums.identityservice.ProvisioningDtos.LinkExistingRequest;
import com.panadi.ums.identityservice.ProvisioningDtos.ProvisionStudentRequest;
import com.panadi.ums.identityservice.ProvisioningDtos.ProvisionTeacherRequest;
import com.panadi.ums.identityservice.ProvisioningDtos.ProvisioningResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/provisioning")
@PreAuthorize("hasRole('ADMIN')")
@Validated
class ProvisioningController {
    private final ProvisioningService service;

    ProvisioningController(ProvisioningService service) { this.service = service; }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    ProvisioningResponse provisionTeacher(@RequestHeader("Idempotency-Key") @NotBlank String key,
                                          @Valid @RequestBody ProvisionTeacherRequest request) {
        return service.provisionTeacher(key, request);
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    ProvisioningResponse provisionStudent(@RequestHeader("Idempotency-Key") @NotBlank String key,
                                          @Valid @RequestBody ProvisionStudentRequest request) {
        return service.provisionStudent(key, request);
    }

    @PostMapping("/teachers/link")
    ProvisioningResponse linkTeacher(@RequestHeader("Idempotency-Key") @NotBlank String key,
                                     @Valid @RequestBody LinkExistingRequest request) {
        return service.linkTeacher(key, request);
    }

    @PostMapping("/students/link")
    ProvisioningResponse linkStudent(@RequestHeader("Idempotency-Key") @NotBlank String key,
                                     @Valid @RequestBody LinkExistingRequest request) {
        return service.linkStudent(key, request);
    }
}

@RestControllerAdvice
class ProvisioningExceptionHandler {
    @ExceptionHandler(ProvisioningConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse conflict(ProvisioningConflictException exception) {
        return new ErrorResponse("PROVISIONING_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(ProvisioningException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ErrorResponse unavailable(ProvisioningException exception) {
        return new ErrorResponse("PROVISIONING_FAILED", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse validation(MethodArgumentNotValidException exception) {
        return new ErrorResponse("VALIDATION_ERROR", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage()).findFirst().orElse("Invalid request"));
    }
}
