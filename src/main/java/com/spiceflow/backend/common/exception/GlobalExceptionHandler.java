package com.spiceflow.backend.common.exception;

import com.spiceflow.backend.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import com.spiceflow.backend.common.dto.ApiResponse;


/** Centralized exception handler — maps all exceptions to standard ErrorResponse JSON. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", safe(ex.getMessage()), Collections.emptyList());
  }

  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<ApiResponse<Object>> handleConflict(
      ResourceConflictException ex, HttpServletRequest request) {
    log.warn("Resource conflict at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", safe(ex.getMessage()), Collections.emptyList());
  }

    @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(
      InvalidCredentialsException ex, HttpServletRequest request) {
    log.warn("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", safe(ex.getMessage()), Collections.emptyList());
  }


    @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ApiResponse<Object>> handleBusinessRule(
      BusinessRuleViolationException ex, HttpServletRequest request) {
    log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", safe(ex.getMessage()), Collections.emptyList());
  }


  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.warn("Validation failed at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
      .getFieldErrors()
      .stream()
      .map(fe -> ErrorResponse.FieldError.builder()
        .field(fe.getField())
        .message(Objects.toString(fe.getDefaultMessage(), ""))
        .build())
      .toList();
    return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", fieldErrors);
  }


    private ResponseEntity<ApiResponse<Object>> buildResponse(
      HttpStatus status, String code, @Nullable String message,
      List<ErrorResponse.FieldError> fieldErrors) {
    String safeMessage = Objects.toString(message, "");
    ErrorResponse error = ErrorResponse.builder()
        .code(code)
        .message(safeMessage)
        .details(fieldErrors)
        .build();
    return ResponseEntity.status(status).body(ApiResponse.error(error));
  }

  private static String safe(@Nullable String s) {
    return Objects.toString(s, "");
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Object>> handleMalformedJson(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.warn("Malformed JSON received at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(
        HttpStatus.BAD_REQUEST, 
        "MALFORMED_JSON",
      "Malformed JSON payload. Please ensure your request body is valid JSON with no unescaped hidden characters.", 
      Collections.emptyList());
  }

    @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have permission to access this resource.", Collections.emptyList());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    log.warn("Unauthenticated request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Full authentication is required to access this resource.", Collections.emptyList());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleAll(Exception ex, HttpServletRequest request) {
    if (ex.getClass().getSimpleName().equals("PropertyReferenceException")) {
      log.warn("Invalid property reference at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
      return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_SORT_PARAMETER", safe(ex.getMessage()), Collections.emptyList());
    }
    log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildResponse(
      HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred", Collections.emptyList());
  }

}
