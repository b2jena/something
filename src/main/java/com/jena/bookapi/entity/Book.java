package com.jena.bookapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Book Entity using modern Java features
 *
 * <p>Interview Points: 1. JPA Entity lifecycle: NEW -> MANAGED -> DETACHED -> REMOVED
 * 2. @EntityListeners for auditing (CreatedDate, LastModifiedDate) 3. Hibernate uses reflection and
 * bytecode enhancement for lazy loading 4. @Version for optimistic locking prevents lost updates
 */
@Entity
@Table(
    name = "books",
    indexes = {
      @Index(name = "idx_book_isbn", columnList = "isbn", unique = true),
      @Index(name = "idx_book_author", columnList = "author"),
      @Index(name = "idx_book_category", columnList = "category")
    })
@EntityListeners(AuditingEntityListener.class)
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Title cannot be blank")
  @Column(nullable = false, length = 255)
  private String title;

  @NotBlank(message = "Author cannot be blank")
  @Column(nullable = false, length = 100)
  private String author;

  @NotBlank(message = "ISBN cannot be blank")
  @Column(nullable = false, unique = true, length = 20)
  private String isbn;

  @NotNull(message = "Price cannot be null")
  @Positive(message = "Price must be positive")
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(length = 50)
  private String category;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "stock_quantity", nullable = false)
  private Integer stockQuantity = 0;

  @Version // Optimistic locking
  private Long version;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Default constructor for JPA
  protected Book() {}

  // Constructor for creating new books
  public Book(
      String title,
      String author,
      String isbn,
      BigDecimal price,
      String category,
      String description,
      Integer stockQuantity) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.price = price;
    this.category = category;
    this.description = description;
    this.stockQuantity = stockQuantity;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getStockQuantity() {
    return stockQuantity;
  }

  public void setStockQuantity(Integer stockQuantity) {
    this.stockQuantity = stockQuantity;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Book book = (Book) o;
    return Objects.equals(isbn, book.isbn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isbn);
  }

  @Override
  public String toString() {
    return "Book{id=%d, title='%s', author='%s', isbn='%s'}".formatted(id, title, author, isbn);
  }
}
