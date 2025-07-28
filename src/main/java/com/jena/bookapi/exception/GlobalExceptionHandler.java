package com.jena.bookapi.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler using @ControllerAdvice
 *
 * <p>Interview Points: 1. @RestControllerAdvice combines @ControllerAdvice + @ResponseBody 2.
 * Centralized exception handling across all controllers 3. ProblemDetail follows RFC 7807 standard
 * for HTTP API error responses 4. Different exception types mapped to appropriate HTTP status codes
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** Handle custom BookNotFoundException */
  @ExceptionHandler(BookNotFoundException.class)
  public ProblemDetail handleBookNotFoundException(BookNotFoundException ex) {
    logger.warn("Book not found: {}", ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Book Not Found");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return problemDetail;
  }

  /** Handle duplicate ISBN exception */
  @ExceptionHandler(DuplicateIsbnException.class)
  public ProblemDetail handleDuplicateIsbnException(DuplicateIsbnException ex) {
    logger.warn("Duplicate ISBN: {}", ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Duplicate ISBN");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return problemDetail;
  }

  /**
   * Handle validation errors from @Valid Interview Point: MethodArgumentNotValidException thrown by
   * Spring validation
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
    logger.warn("Validation failed: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problemDetail.setTitle("Validation Error");
    problemDetail.setProperty("timestamp", LocalDateTime.now());
    problemDetail.setProperty("errors", errors);

    return problemDetail;
  }

  /** Handle constraint violations */
  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
    logger.warn("Constraint violation: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      String propertyPath = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      errors.put(propertyPath, message);
    }

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Constraint violation");
    problemDetail.setTitle("Constraint Violation");
    problemDetail.setProperty("timestamp", LocalDateTime.now());
    problemDetail.setProperty("errors", errors);

    return problemDetail;
  }

  /** Handle security access denied */
  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
    logger.warn("Access denied: {}", ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
    problemDetail.setTitle("Access Denied");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return problemDetail;
  }

  /**
   * Handle all other exceptions Interview Point: Catch-all handler prevents stack traces from
   * leaking to clients
   */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex) {
    logger.error("Unexpected error occurred", ex);

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    return problemDetail;
  }
}
