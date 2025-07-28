package com.jena.bookapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jena.bookapi.dto.BookRequest;
import com.jena.bookapi.dto.BookResponse;
import com.jena.bookapi.entity.Book;
import com.jena.bookapi.exception.BookNotFoundException;
import com.jena.bookapi.exception.DuplicateIsbnException;
import com.jena.bookapi.mapper.BookMapper;
import com.jena.bookapi.repository.BookRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit Tests for BookService
 *
 * <p>Interview Points: 1. @ExtendWith(MockitoExtension.class) enables Mockito annotations 2. @Mock
 * creates mock objects, @InjectMocks injects mocks into the service 3. AssertJ provides fluent
 * assertions for better readability 4. Test method naming follows Given-When-Then pattern
 * 5. @DisplayName provides human-readable test descriptions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

  @Mock private BookRepository bookRepository;

  @Mock private BookMapper bookMapper;

  @InjectMocks private BookService bookService;

  private Book testBook;
  private BookRequest testBookRequest;
  private BookResponse testBookResponse;

  @BeforeEach
  void setUp() {
    testBook =
        new Book(
            "Test Title",
            "Test Author",
            "1234567890",
            new BigDecimal("29.99"),
            "Fiction",
            "Test Description",
            10);
    testBook.setId(1L);
    testBook.setCreatedAt(LocalDateTime.now());
    testBook.setUpdatedAt(LocalDateTime.now());

    testBookRequest =
        new BookRequest(
            "Test Title",
            "Test Author",
            "1234567890",
            new BigDecimal("29.99"),
            "Fiction",
            "Test Description",
            10);

    testBookResponse =
        new BookResponse(
            1L,
            "Test Title",
            "Test Author",
            "1234567890",
            new BigDecimal("29.99"),
            "Fiction",
            "Test Description",
            10,
            0L,
            LocalDateTime.now(),
            LocalDateTime.now());
  }

  @Test
  @DisplayName("Should return all books with pagination")
  void getAllBooks_ShouldReturnPagedBooks() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);

    when(bookRepository.findAll(pageable)).thenReturn(bookPage);
    when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

    // When
    Page<BookResponse> result = bookService.getAllBooks(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0)).isEqualTo(testBookResponse);

    verify(bookRepository).findAll(pageable);
    verify(bookMapper).toResponse(testBook);
  }

  @Test
  @DisplayName("Should return book by ID when book exists")
  void getBookById_WhenBookExists_ShouldReturnBook() {
    // Given
    Long bookId = 1L;
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));
    when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

    // When
    BookResponse result = bookService.getBookById(bookId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(testBookResponse);

    verify(bookRepository).findById(bookId);
    verify(bookMapper).toResponse(testBook);
  }

  @Test
  @DisplayName("Should throw BookNotFoundException when book does not exist")
  void getBookById_WhenBookNotExists_ShouldThrowException() {
    // Given
    Long bookId = 999L;
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> bookService.getBookById(bookId))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessage("Book not found with ID: " + bookId);

    verify(bookRepository).findById(bookId);
    verifyNoInteractions(bookMapper);
  }

  @Test
  @DisplayName("Should create book when ISBN is unique")
  void createBook_WhenIsbnIsUnique_ShouldCreateBook() {
    // Given
    when(bookRepository.existsByIsbn(testBookRequest.isbn())).thenReturn(false);
    when(bookMapper.toEntity(testBookRequest)).thenReturn(testBook);
    when(bookRepository.save(testBook)).thenReturn(testBook);
    when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

    // When
    BookResponse result = bookService.createBook(testBookRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(testBookResponse);

    verify(bookRepository).existsByIsbn(testBookRequest.isbn());
    verify(bookMapper).toEntity(testBookRequest);
    verify(bookRepository).save(testBook);
    verify(bookMapper).toResponse(testBook);
  }

  @Test
  @DisplayName("Should throw DuplicateIsbnException when ISBN already exists")
  void createBook_WhenIsbnExists_ShouldThrowException() {
    // Given
    when(bookRepository.existsByIsbn(testBookRequest.isbn())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> bookService.createBook(testBookRequest))
        .isInstanceOf(DuplicateIsbnException.class)
        .hasMessage("Book with ISBN " + testBookRequest.isbn() + " already exists");

    verify(bookRepository).existsByIsbn(testBookRequest.isbn());
    verifyNoMoreInteractions(bookRepository, bookMapper);
  }

  @Test
  @DisplayName("Should update book when book exists and ISBN is unique")
  void updateBook_WhenValidRequest_ShouldUpdateBook() {
    // Given
    Long bookId = 1L;
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));
    when(bookRepository.findByIsbn(testBookRequest.isbn())).thenReturn(Optional.empty());
    when(bookRepository.save(testBook)).thenReturn(testBook);
    when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

    // When
    BookResponse result = bookService.updateBook(bookId, testBookRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(testBookResponse);

    verify(bookRepository).findById(bookId);
    verify(bookRepository).findByIsbn(testBookRequest.isbn());
    verify(bookMapper).updateEntity(testBook, testBookRequest);
    verify(bookRepository).save(testBook);
    verify(bookMapper).toResponse(testBook);
  }

  @Test
  @DisplayName("Should delete book when book exists")
  void deleteBook_WhenBookExists_ShouldDeleteBook() {
    // Given
    Long bookId = 1L;
    when(bookRepository.existsById(bookId)).thenReturn(true);

    // When
    bookService.deleteBook(bookId);

    // Then
    verify(bookRepository).existsById(bookId);
    verify(bookRepository).deleteById(bookId);
  }

  @Test
  @DisplayName("Should throw BookNotFoundException when deleting non-existent book")
  void deleteBook_WhenBookNotExists_ShouldThrowException() {
    // Given
    Long bookId = 999L;
    when(bookRepository.existsById(bookId)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> bookService.deleteBook(bookId))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessage("Book not found with ID: " + bookId);

    verify(bookRepository).existsById(bookId);
    verify(bookRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should search books by title or author")
  void searchBooks_ShouldReturnMatchingBooks() {
    // Given
    String searchTerm = "test";
    Pageable pageable = PageRequest.of(0, 10);
    Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);

    when(bookRepository.searchByTitleOrAuthor(searchTerm, pageable)).thenReturn(bookPage);
    when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

    // When
    Page<BookResponse> result = bookService.searchBooks(searchTerm, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0)).isEqualTo(testBookResponse);

    verify(bookRepository).searchByTitleOrAuthor(searchTerm, pageable);
    verify(bookMapper).toResponse(testBook);
  }
}
