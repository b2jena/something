package com.jena.bookapi.controller;

import com.jena.bookapi.dto.BookRequest;
import com.jena.bookapi.dto.BookResponse;
import com.jena.bookapi.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller for Book operations with HATEOAS support
 *
 * <p>Interview Points: 1. @RestController combines @Controller + @ResponseBody 2. @RequestMapping
 * defines base path and common attributes 3. HATEOAS provides hypermedia links for API
 * discoverability 4. @Valid triggers Bean Validation on request bodies 5. ResponseEntity allows
 * full control over HTTP response
 */
@RestController
@RequestMapping("/api/v1/books")
@Validated
@Tag(name = "Books", description = "Book management operations")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Get all books with pagination and HATEOAS links Interview Point: @PageableDefault provides
     * sensible defaults for pagination
     */
    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve paginated list of books")
    @ApiResponse(responseCode = "200", description = "Books retrieved successfully")
    public ResponseEntity<PagedModel<EntityModel<BookResponse>>> getAllBooks(
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {

        logger.info(
                "GET /api/v1/books - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<BookResponse> books = bookService.getAllBooks(pageable);

        // Convert to HATEOAS PagedModel
        PagedModel<EntityModel<BookResponse>> pagedModel =
                PagedModel.of(
                        books.getContent().stream()
                                .map(
                                        book ->
                                                EntityModel.of(book)
                                                        .add(
                                                                linkTo(methodOn(BookController.class).getBookById(book.id()))
                                                                        .withSelfRel())
                                                        .add(
                                                                linkTo(methodOn(BookController.class).updateBook(book.id(), null))
                                                                        .withRel("update"))
                                                        .add(
                                                                linkTo(methodOn(BookController.class).deleteBook(book.id()))
                                                                        .withRel("delete")))
                                .toList(),
                        new PagedModel.PageMetadata(
                                books.getSize(),
                                books.getNumber(),
                                books.getTotalElements(),
                                books.getTotalPages()));

        // Add navigation links
        pagedModel.add(linkTo(methodOn(BookController.class).getAllBooks(pageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    /**
     * Get book by ID with HATEOAS links
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its ID")
    @ApiResponse(responseCode = "200", description = "Book found")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<EntityModel<BookResponse>> getBookById(
            @Parameter(description = "Book ID") @PathVariable Long id) {

        logger.info("GET /api/v1/books/{}", id);

        BookResponse book = bookService.getBookById(id);

        EntityModel<BookResponse> bookModel =
                EntityModel.of(book)
                        .add(linkTo(methodOn(BookController.class).getBookById(id)).withSelfRel())
                        .add(
                                linkTo(methodOn(BookController.class).getAllBooks(Pageable.unpaged()))
                                        .withRel("books"))
                        .add(linkTo(methodOn(BookController.class).updateBook(id, null)).withRel("update"))
                        .add(linkTo(methodOn(BookController.class).deleteBook(id)).withRel("delete"));

        return ResponseEntity.ok(bookModel);
    }

    /**
     * Create new book Interview Point: @Valid triggers validation, @RequestBody deserializes JSON
     */
    @PostMapping
    @Operation(summary = "Create new book", description = "Create a new book in the system")
    @ApiResponse(responseCode = "201", description = "Book created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Book with ISBN already exists")
    public ResponseEntity<EntityModel<BookResponse>> createBook(
            @Valid @RequestBody BookRequest request) {

        logger.info("POST /api/v1/books - ISBN: {}", request.isbn());

        BookResponse createdBook = bookService.createBook(request);

        EntityModel<BookResponse> bookModel =
                EntityModel.of(createdBook)
                        .add(linkTo(methodOn(BookController.class).getBookById(createdBook.id())).withSelfRel())
                        .add(
                                linkTo(methodOn(BookController.class).getAllBooks(Pageable.unpaged()))
                                        .withRel("books"));

        return ResponseEntity.status(HttpStatus.CREATED).body(bookModel);
    }

    /**
     * Update existing book
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Update an existing book")
    @ApiResponse(responseCode = "200", description = "Book updated successfully")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @ApiResponse(responseCode = "409", description = "ISBN conflict")
    public ResponseEntity<EntityModel<BookResponse>> updateBook(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {

        logger.info("PUT /api/v1/books/{}", id);

        BookResponse updatedBook = bookService.updateBook(id, request);

        EntityModel<BookResponse> bookModel =
                EntityModel.of(updatedBook)
                        .add(linkTo(methodOn(BookController.class).getBookById(id)).withSelfRel())
                        .add(
                                linkTo(methodOn(BookController.class).getAllBooks(Pageable.unpaged()))
                                        .withRel("books"));

        return ResponseEntity.ok(bookModel);
    }

    /**
     * Delete book
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Delete a book from the system")
    @ApiResponse(responseCode = "204", description = "Book deleted successfully")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Book ID") @PathVariable Long id) {

        logger.info("DELETE /api/v1/books/{}", id);

        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search books by title or author
     */
    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search books by title or author")
    public ResponseEntity<PagedModel<EntityModel<BookResponse>>> searchBooks(
            @Parameter(description = "Search term") @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {

        logger.info("GET /api/v1/books/search?q={}", q);

        Page<BookResponse> books = bookService.searchBooks(q, pageable);

        PagedModel<EntityModel<BookResponse>> pagedModel =
                PagedModel.of(
                        books.getContent().stream()
                                .map(
                                        book ->
                                                EntityModel.of(book)
                                                        .add(
                                                                linkTo(methodOn(BookController.class).getBookById(book.id()))
                                                                        .withSelfRel()))
                                .toList(),
                        new PagedModel.PageMetadata(
                                books.getSize(),
                                books.getNumber(),
                                books.getTotalElements(),
                                books.getTotalPages()));

        return ResponseEntity.ok(pagedModel);
    }

    /**
     * Get books by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get books by category", description = "Retrieve books filtered by category")
    public ResponseEntity<PagedModel<EntityModel<BookResponse>>> getBooksByCategory(
            @Parameter(description = "Book category") @PathVariable String category,
            @PageableDefault(size = 20) Pageable pageable) {

        logger.info("GET /api/v1/books/category/{}", category);

        Page<BookResponse> books = bookService.getBooksByCategory(category, pageable);

        PagedModel<EntityModel<BookResponse>> pagedModel =
                PagedModel.of(
                        books.getContent().stream()
                                .map(
                                        book ->
                                                EntityModel.of(book)
                                                        .add(
                                                                linkTo(methodOn(BookController.class).getBookById(book.id()))
                                                                        .withSelfRel()))
                                .toList(),
                        new PagedModel.PageMetadata(
                                books.getSize(),
                                books.getNumber(),
                                books.getTotalElements(),
                                books.getTotalPages()));

        return ResponseEntity.ok(pagedModel);
    }

    /**
     * Get books with low stock (async operation) Interview Point: CompletableFuture enables
     * non-blocking async operations
     */
    @GetMapping("/low-stock")
    @Operation(
            summary = "Get low stock books",
            description = "Retrieve books with stock below threshold (async)")
    public CompletableFuture<ResponseEntity<List<BookResponse>>> getLowStockBooks(
            @Parameter(description = "Stock threshold") @RequestParam @Min(1) Integer threshold) {

        logger.info("GET /api/v1/books/low-stock?threshold={}", threshold);

        return bookService.getLowStockBooksAsync(threshold).thenApply(ResponseEntity::ok);
    }
}
