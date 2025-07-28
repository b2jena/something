package com.jena.bookapi.exception;

/**
 * Custom exception for book not found scenarios
 *
 * <p>Interview Points: 1. Custom exceptions provide better error handling and debugging 2.
 * RuntimeException doesn't require try-catch (unchecked exception) 3. Used with @ControllerAdvice
 * for global exception handling
 */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }

    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
