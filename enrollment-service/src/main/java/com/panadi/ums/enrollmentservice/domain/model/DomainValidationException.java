package com.panadi.ums.enrollmentservice.domain.model;

public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message) {
        super(message);
    }
}
