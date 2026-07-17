package com.panadi.ums.attendanceservice.domain.model;

public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message) {
        super(message);
    }
}
