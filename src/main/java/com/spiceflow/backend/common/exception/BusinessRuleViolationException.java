package com.spiceflow.backend.common.exception;

/** Thrown when a business rule is violated (e.g insuficient stock). Maps to HTTP 422. */

public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }

}
