package com.jena.bookapi.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Book Request DTO using Java 17 Records
 *
 * <p>Interview Points: 1. Records are immutable data carriers (final fields, no setters) 2.
 * Automatically generates constructor, equals(), hashCode(), toString() 3. Compact constructor
 * allows validation logic 4. Records can implement interfaces and have static methods
 */
public record BookRequest(
    @NotBlank(message = "Title is required")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,
    @NotBlank(message = "Author is required")
        @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
        String author,
    @NotBlank(message = "ISBN is required")
        @Pattern(
            regexp =
                "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
            message = "Invalid ISBN format")
        String isbn,
    @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @DecimalMax(value = "9999.99", message = "Price must be less than 10000")
        @Digits(
            integer = 4,
            fraction = 2,
            message = "Price must have at most 4 integer digits and 2 decimal places")
        BigDecimal price,
    @Size(max = 50, message = "Category must be at most 50 characters") String category,
    @Size(max = 1000, message = "Description must be at most 1000 characters") String description,
    @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        @Max(value = 10000, message = "Stock quantity cannot exceed 10000")
        Integer stockQuantity) {
  /**
   * Compact constructor for additional validation Interview Point: Compact constructors run before
   * field assignment
   */
  public BookRequest {
    // Normalize strings
    if (title != null) {
      title = title.trim();
    }
    if (author != null) {
      author = author.trim();
    }
    if (isbn != null) {
      isbn = isbn.replaceAll("[^0-9X]", "").toUpperCase();
    }
    if (category != null) {
      category = category.trim();
    }
    if (description != null) {
      description = description.trim();
    }
  }
}
