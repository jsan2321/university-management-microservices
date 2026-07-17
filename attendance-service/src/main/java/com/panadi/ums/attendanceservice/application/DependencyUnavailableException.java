package com.panadi.ums.attendanceservice.application;

public class DependencyUnavailableException extends RuntimeException {
    public DependencyUnavailableException(String message) {
        super(message);
    }
}
