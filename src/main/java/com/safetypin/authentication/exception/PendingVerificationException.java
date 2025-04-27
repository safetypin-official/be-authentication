package com.safetypin.authentication.exception;

public class PendingVerificationException extends RuntimeException {
    public PendingVerificationException(String message) {
        super(message);
    }
}
