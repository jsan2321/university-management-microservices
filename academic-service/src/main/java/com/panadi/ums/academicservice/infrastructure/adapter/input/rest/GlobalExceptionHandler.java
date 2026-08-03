package com.panadi.ums.academicservice.infrastructure.adapter.input.rest;

import com.panadi.ums.academicservice.application.ApplicationException;
import com.panadi.ums.academicservice.application.ResourceNotFoundException;
import com.panadi.ums.academicservice.domain.model.DomainValidationException;
import com.panadi.ums.security.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return ApiErrorResponse.of("RESOURCE_NOT_FOUND", exception.getMessage(),
                HttpStatus.NOT_FOUND.value(), request.getRequestURI());
    }

    @ExceptionHandler({ApplicationException.class, DomainValidationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiErrorResponse handleBusinessRule(RuntimeException exception, HttpServletRequest request) {
        return ApiErrorResponse.of("BUSINESS_RULE_VIOLATION", exception.getMessage(),
                HttpStatus.CONFLICT.value(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiErrorResponse.of("VALIDATION_ERROR", message,
                HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
    }
}
