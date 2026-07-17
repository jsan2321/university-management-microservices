package com.panadi.ums.studentservice.application;

public class DependencyUnavailableException extends RuntimeException {
    public DependencyUnavailableException(String message) {
        super(message);
    }
}
