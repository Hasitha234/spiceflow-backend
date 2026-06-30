package com.spiceflow.backend.common.exception;

/** Thrown when login credentials are wrong or account is locked. Maps to HTTP 401. */

public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }

}
