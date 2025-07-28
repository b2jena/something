package com.jena.bookapi.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jena.bookapi.config.TestCacheConfig;
import com.jena.bookapi.config.TestSecurityConfig;
import com.jena.bookapi.dto.BookRequest;
import com.jena.bookapi.entity.Book;
import com.jena.bookapi.repository.BookRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration Tests using Testcontainers
 *
 * <p>Interview Points: 1. @SpringBootTest loads full application context 2. Testcontainers provides
 * real database for integration testing 3. @DynamicPropertySource configures test properties
 * dynamically 4. @WithMockUser provides security context for testing 5. @Transactional ensures test
 * isolation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestCacheConfig.class})
@TestPropertySource(
    properties = {"spring.cache.type=simple", "spring.data.redis.repositories.enabled=false"})
@Transactional
@DisplayName("Book API Integration Tests")
class BookApiIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private BookRepository bookRepository;

  private BookRequest testBookRequest;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    bookRepository.deleteAll();
    testBookRequest =
        new BookRequest(
            "Integration Test Book",
            "Test Author",
            "9781234567890",
            new BigDecimal("39.99"),
            "Technology",
            "A comprehensive guide to integration testing",
            50);
  }

  @Test
  @DisplayName("Should create book successfully with valid request")
  @WithMockUser(roles = "ADMIN")
  void createBook_WithValidRequest_ShouldReturnCreated() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title", is(testBookRequest.title())))
        .andExpect(jsonPath("$.author", is(testBookRequest.author())))
        .andExpect(jsonPath("$.isbn", is(testBookRequest.isbn())))
        .andExpect(jsonPath("$.price", is(testBookRequest.price().doubleValue())))
        .andExpect(jsonPath("$.category", is(testBookRequest.category())))
        .andExpect(jsonPath("$.stockQuantity", is(testBookRequest.stockQuantity())))
        .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/books/")));
  }

  @Test
  @DisplayName("Should return validation error for invalid request")
  @WithMockUser(roles = "ADMIN")
  void createBook_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
    BookRequest invalidRequest =
        new BookRequest(
            "", // Empty title
            "Test Author",
            "invalid-isbn",
            new BigDecimal("-10.00"), // Negative price
            "Technology",
            "Test Description",
            -5 // Negative stock
            );

    mockMvc
        .perform(
            post("/api/v1/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title", is("Validation Error")))
        .andExpect(jsonPath("$.errors", notNullValue()));
  }

  @Test
  @DisplayName("Should get book by ID successfully")
  @WithMockUser(roles = "USER")
  void getBookById_WhenBookExists_ShouldReturnBook() throws Exception {
    // Create a book first
    Book savedBook =
        bookRepository.save(
            new Book(
                testBookRequest.title(),
                testBookRequest.author(),
                testBookRequest.isbn(),
                testBookRequest.price(),
                testBookRequest.category(),
                testBookRequest.description(),
                testBookRequest.stockQuantity()));

    mockMvc
        .perform(get("/api/v1/books/{id}", savedBook.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(savedBook.getId().intValue())))
        .andExpect(jsonPath("$.title", is(testBookRequest.title())))
        .andExpect(jsonPath("$.author", is(testBookRequest.author())))
        .andExpect(
            jsonPath("$._links.self.href", containsString("/api/v1/books/" + savedBook.getId())));
  }

  @Test
  @DisplayName("Should return 404 for non-existent book")
  @WithMockUser(roles = "USER")
  void getBookById_WhenBookNotExists_ShouldReturnNotFound() throws Exception {
    mockMvc
        .perform(get("/api/v1/books/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title", is("Book Not Found")))
        .andExpect(jsonPath("$.detail", containsString("Book not found with ID: 999")));
  }

  @Test
  @DisplayName("Should update book successfully")
  @WithMockUser(roles = "ADMIN")
  void updateBook_WithValidRequest_ShouldReturnUpdatedBook() throws Exception {
    // Create a book first
    Book savedBook =
        bookRepository.save(
            new Book(
                "Original Title",
                "Original Author",
                "9780123456789",
                new BigDecimal("29.99"),
                "Fiction",
                "Original Description",
                25));

    mockMvc
        .perform(
            put("/api/v1/books/{id}", savedBook.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title", is(testBookRequest.title())))
        .andExpect(jsonPath("$.author", is(testBookRequest.author())))
        .andExpect(jsonPath("$.isbn", is(testBookRequest.isbn())));
  }

  @Test
  @DisplayName("Should delete book successfully")
  @WithMockUser(roles = "ADMIN")
  void deleteBook_WhenBookExists_ShouldReturnNoContent() throws Exception {
    // Create a book first
    Book savedBook =
        bookRepository.save(
            new Book(
                testBookRequest.title(),
                testBookRequest.author(),
                testBookRequest.isbn(),
                testBookRequest.price(),
                testBookRequest.category(),
                testBookRequest.description(),
                testBookRequest.stockQuantity()));

    mockMvc
        .perform(delete("/api/v1/books/{id}", savedBook.getId()).with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should search books successfully")
  @WithMockUser(roles = "USER")
  void searchBooks_WithValidTerm_ShouldReturnMatchingBooks() throws Exception {
    // Create test books
    bookRepository.save(
        new Book(
            "Spring Boot Guide",
            "John Doe",
            "9781111111111",
            new BigDecimal("49.99"),
            "Technology",
            "Comprehensive Spring Boot guide",
            30));

    bookRepository.save(
        new Book(
            "Java Fundamentals",
            "Jane Smith",
            "9782222222222",
            new BigDecimal("39.99"),
            "Technology",
            "Learn Java basics",
            20));

    mockMvc
        .perform(get("/api/v1/books/search").param("q", "Spring"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.bookResponseList", hasSize(1)))
        .andExpect(jsonPath("$._embedded.bookResponseList[0].title", containsString("Spring")));
  }

  @Test
  @DisplayName("Should require authentication for protected endpoints")
  void protectedEndpoint_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should require proper role for admin operations")
  @WithMockUser(roles = "USER")
  void adminOperation_WithUserRole_ShouldReturnForbidden() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookRequest)))
        .andExpect(status().isForbidden());
  }
}
