package com.spiceflow.backend.common.exception;
import lombok.Builder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Centralized exception handler — maps all exceptions to standard RFC 7807 ProblemDetails. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  

  private ProblemDetail buildProblemDetail(
      HttpStatus status, 
      String title, 
      String detail, 
      HttpServletRequest request, 
      List<FieldErrorInfo> errors) {
    
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setTitle(title);
    problemDetail.setInstance(URI.create(request.getRequestURI()));

    // Enrich with traceId and timestamp as requested
    String traceId = MDC.get("traceId");
    if (traceId == null) {
      traceId = UUID.randomUUID().toString(); // Fallback if no tracing filter exists yet
    }
    
    problemDetail.setProperty("traceId", traceId);
    problemDetail.setProperty("timestamp", OffsetDateTime.now(java.time.ZoneId.systemDefault()).toString());

    if (errors != null && !errors.isEmpty()) {
      problemDetail.setProperty("errors", errors);
    }

    return problemDetail;
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", safe(ex.getMessage()), request, Collections.emptyList()));
  }

  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<ProblemDetail> handleConflict(
      ResourceConflictException ex, HttpServletRequest request) {
    log.warn("Resource conflict at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(buildProblemDetail(HttpStatus.CONFLICT, "Resource Conflict", safe(ex.getMessage()), request, Collections.emptyList()));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleInvalidCredentials(
      InvalidCredentialsException ex, HttpServletRequest request) {
    log.warn("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(buildProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid Credentials", safe(ex.getMessage()), request, Collections.emptyList()));
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ProblemDetail> handleBusinessRule(
      BusinessRuleViolationException ex, HttpServletRequest request) {
    log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(buildProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Business Rule Violation", safe(ex.getMessage()), request, Collections.emptyList()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.warn("Validation failed at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    List<FieldErrorInfo> fieldErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fe -> new FieldErrorInfo(fe.getField(), Objects.toString(fe.getDefaultMessage(), "")))
        .toList();
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation Error", "Request validation failed", request, fieldErrors));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleMalformedJson(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.warn("Malformed JSON received at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(buildProblemDetail(HttpStatus.BAD_REQUEST, "Malformed JSON", "Malformed JSON payload. Ensure your request body is valid.", request, Collections.emptyList()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(buildProblemDetail(HttpStatus.FORBIDDEN, "Access Denied", "You do not have permission to access this resource.", request, Collections.emptyList()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    log.warn("Unauthenticated request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(buildProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", "Full authentication is required to access this resource.", request, Collections.emptyList()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleAll(Exception ex, HttpServletRequest request) {
    if (ex.getClass().getSimpleName().equals("PropertyReferenceException")) {
      log.warn("Invalid property reference at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Parameter", safe(ex.getMessage()), request, Collections.emptyList()));
    }
    log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", request, Collections.emptyList()));
  }

  private static String safe(@Nullable String s) {
    return Objects.toString(s, "");
  }

  @Builder
public record FieldErrorInfo(String field, String message) {}
}