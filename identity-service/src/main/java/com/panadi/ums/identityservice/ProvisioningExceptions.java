package com.panadi.ums.identityservice;

class ProvisioningException extends RuntimeException {
    ProvisioningException(String message) { super(message); }
    ProvisioningException(String message, Throwable cause) { super(message, cause); }
}

class ProvisioningConflictException extends ProvisioningException {
    ProvisioningConflictException(String message) { super(message); }
}
