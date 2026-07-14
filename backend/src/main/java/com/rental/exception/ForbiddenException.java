package com.rental.exception;

/**
 * Thrown when a request has valid credentials but the authenticated
 * user isn't allowed to perform this action (wrong owner, wrong role).
 * Maps to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}