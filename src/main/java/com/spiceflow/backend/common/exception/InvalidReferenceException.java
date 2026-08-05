package com.spiceflow.backend.common.exception;

/**
 * Exception thrown when a referenced entity (such as a foreign key in a request payload)
 * cannot be found. This should be used for data validation failures, which are mapped to 
 * 422 Unprocessable Entity, avoiding confusion with standard 404 Endpoint Not Found errors.
 */
public class InvalidReferenceException extends RuntimeException {
    public InvalidReferenceException(String message) {
        super(message);
    }

    public InvalidReferenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
