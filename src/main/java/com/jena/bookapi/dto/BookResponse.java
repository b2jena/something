package com.jena.bookapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Book Response DTO extending HATEOAS RepresentationModel
 *
 * <p>Interview Points: 1. HATEOAS (Hypermedia as the Engine of Application State) provides
 * navigation links 2. RepresentationModel adds _links field for hypermedia controls 3. @JsonFormat
 * ensures consistent date serialization across timezones 4. Immutable response objects prevent
 * accidental modification
 */
public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        BigDecimal price,
        String category,
        String description,
        Integer stockQuantity,
        Long version,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt) {

    /**
     * HATEOAS-enabled response wrapper Interview Point: Extends RepresentationModel to include
     * hypermedia links
     */
    public static class BookHateoasResponse extends RepresentationModel<BookHateoasResponse> {
        private final BookResponse book;

        public BookHateoasResponse(BookResponse book) {
            this.book = book;
        }

        public BookResponse getBook() {
            return book;
        }

        // Delegate methods for JSON serialization
        public Long getId() {
            return book.id();
        }

        public String getTitle() {
            return book.title();
        }

        public String getAuthor() {
            return book.author();
        }

        public String getIsbn() {
            return book.isbn();
        }

        public BigDecimal getPrice() {
            return book.price();
        }

        public String getCategory() {
            return book.category();
        }

        public String getDescription() {
            return book.description();
        }

        public Integer getStockQuantity() {
            return book.stockQuantity();
        }

        public Long getVersion() {
            return book.version();
        }

        public LocalDateTime getCreatedAt() {
            return book.createdAt();
        }

        public LocalDateTime getUpdatedAt() {
            return book.updatedAt();
        }
    }
}
