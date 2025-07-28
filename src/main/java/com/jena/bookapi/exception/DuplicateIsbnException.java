package com.jena.bookapi.exception;

/**
 * Custom exception for duplicate ISBN scenarios
 */
public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(String message) {
        super(message);
    }

    public DuplicateIsbnException(String message, Throwable cause) {
        super(message, cause);
    }
}
