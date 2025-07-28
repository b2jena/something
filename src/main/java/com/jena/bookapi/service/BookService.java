package com.jena.bookapi.service;

import com.jena.bookapi.dto.BookRequest;
import com.jena.bookapi.dto.BookResponse;
import com.jena.bookapi.entity.Book;
import com.jena.bookapi.exception.BookNotFoundException;
import com.jena.bookapi.exception.DuplicateIsbnException;
import com.jena.bookapi.mapper.BookMapper;
import com.jena.bookapi.repository.BookRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Book Service Layer with comprehensive business logic
 *
 * <p>Interview Points: 1. @Service is a specialization of @Component for business logic layer
 * 2. @Transactional ensures ACID properties and automatic rollback on exceptions
 * 3. @Cacheable/@CacheEvict improve performance by caching frequently accessed data
 * 4. @PreAuthorize provides method-level security based on SpEL expressions 5. @Async enables
 * non-blocking operations with CompletableFuture
 */
@Service
@Transactional(readOnly = true) // Default to read-only transactions for better performance
public class BookService {

  private static final Logger logger = LoggerFactory.getLogger(BookService.class);

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;

  public BookService(BookRepository bookRepository, BookMapper bookMapper) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
  }

  /**
   * Get all books with pagination Interview Point: @Cacheable caches results based on method
   * parameters
   */
  @Cacheable(value = "books", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
  public Page<BookResponse> getAllBooks(Pageable pageable) {
    logger.debug("Fetching books with pagination: {}", pageable);
    return bookRepository.findAll(pageable).map(bookMapper::toResponse);
  }

  /** Get book by ID Interview Point: @Cacheable with condition prevents caching null results */
  @Cacheable(value = "book", key = "#id", condition = "#result != null")
  public BookResponse getBookById(Long id) {
    MDC.put("bookId", String.valueOf(id));
    try {
      logger.info("Fetching book by ID: {}", id);
      Book book =
          bookRepository
              .findById(id)
              .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
      return bookMapper.toResponse(book);
    } finally {
      MDC.remove("bookId");
    }
  }

  /** Create new book Interview Point: @Transactional without readOnly enables write operations */
  @Transactional
  @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
  @CacheEvict(value = "books", allEntries = true) // Clear cache after modification
  public BookResponse createBook(BookRequest request) {
    logger.info("Creating new book with ISBN: {}", request.isbn());

    // Business validation
    if (bookRepository.existsByIsbn(request.isbn())) {
      throw new DuplicateIsbnException("Book with ISBN " + request.isbn() + " already exists");
    }

    Book book = bookMapper.toEntity(request);
    Book savedBook = bookRepository.save(book);

    logger.info("Successfully created book with ID: {}", savedBook.getId());
    return bookMapper.toResponse(savedBook);
  }

  /**
   * Update existing book Interview Point: Optimistic locking prevents lost updates through @Version
   */
  @Transactional
  @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
  @CacheEvict(
      value = {"book", "books"},
      key = "#id",
      allEntries = true)
  public BookResponse updateBook(Long id, BookRequest request) {
    logger.info("Updating book with ID: {}", id);

    Book existingBook =
        bookRepository
            .findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

    // Check for ISBN conflicts (excluding current book)
    bookRepository
        .findByIsbn(request.isbn())
        .filter(book -> !book.getId().equals(id))
        .ifPresent(
            book -> {
              throw new DuplicateIsbnException("ISBN " + request.isbn() + " is already in use");
            });

    bookMapper.updateEntity(existingBook, request);
    Book updatedBook = bookRepository.save(existingBook);

    logger.info("Successfully updated book with ID: {}", id);
    return bookMapper.toResponse(updatedBook);
  }

  /** Delete book Interview Point: @PreAuthorize restricts access to admin users only */
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  @CacheEvict(
      value = {"book", "books"},
      key = "#id",
      allEntries = true)
  public void deleteBook(Long id) {
    logger.info("Deleting book with ID: {}", id);

    if (!bookRepository.existsById(id)) {
      throw new BookNotFoundException("Book not found with ID: " + id);
    }

    bookRepository.deleteById(id);
    logger.info("Successfully deleted book with ID: {}", id);
  }

  /** Search books by title or author */
  public Page<BookResponse> searchBooks(String searchTerm, Pageable pageable) {
    logger.debug("Searching books with term: {}", searchTerm);
    return bookRepository.searchByTitleOrAuthor(searchTerm, pageable).map(bookMapper::toResponse);
  }

  /** Get books by category */
  @Cacheable(value = "booksByCategory", key = "#category + '-' + #pageable.pageNumber")
  public Page<BookResponse> getBooksByCategory(String category, Pageable pageable) {
    logger.debug("Fetching books by category: {}", category);
    return bookRepository.findByCategory(category, pageable).map(bookMapper::toResponse);
  }

  /**
   * Async method for bulk operations Interview Point: @Async runs in separate thread pool, returns
   * CompletableFuture
   */
  @Async
  @PreAuthorize("hasRole('ADMIN')")
  public CompletableFuture<List<BookResponse>> getLowStockBooksAsync(Integer threshold) {
    logger.info("Async: Fetching books with low stock (threshold: {})", threshold);

    List<Book> lowStockBooks = bookRepository.findBooksWithLowStock(threshold);
    List<BookResponse> responses = lowStockBooks.stream().map(bookMapper::toResponse).toList();

    logger.info("Async: Found {} books with low stock", responses.size());
    return CompletableFuture.completedFuture(responses);
  }
}
