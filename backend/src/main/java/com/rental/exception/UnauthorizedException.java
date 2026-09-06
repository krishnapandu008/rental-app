package com.rental.exception;

/**
 * Thrown when a request has no valid credentials at all
 * (missing/invalid JWT, bad login). Maps to HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnauthorizedException(String message) {
        super(message);
    }
}