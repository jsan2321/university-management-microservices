package com.panadi.ums.studentservice.infrastructure.adapter.input.rest;

import com.panadi.ums.studentservice.application.ApplicationException;
import com.panadi.ums.studentservice.application.DependencyUnavailableException;
import com.panadi.ums.studentservice.application.ResourceNotFoundException;
import com.panadi.ums.studentservice.domain.model.DomainValidationException;
import com.panadi.ums.studentservice.infrastructure.adapter.input.rest.dto.StudentDtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNotFound(ResourceNotFoundException exception) {
        return new ErrorResponse("RESOURCE_NOT_FOUND", exception.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler({ApplicationException.class, DomainValidationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleBusinessRule(RuntimeException exception) {
        return new ErrorResponse("BUSINESS_RULE_VIOLATION", exception.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(DependencyUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ErrorResponse handleDependency(DependencyUnavailableException exception) {
        return new ErrorResponse("DEPENDENCY_UNAVAILABLE", exception.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ErrorResponse("VALIDATION_ERROR", message, LocalDateTime.now());
    }
}
