package com.jena.bookapi.repository;

import com.jena.bookapi.entity.Book;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Book Repository using Spring Data JPA
 *
 * <p>Interview Points: 1. @Repository is a specialization of @Component for data access layer 2.
 * JpaRepository provides CRUD operations and pagination 3. Spring Data JPA generates implementation
 * at runtime using proxies 4. Query methods follow naming conventions for automatic query
 * generation 5. @Query allows custom JPQL/SQL queries with parameter binding
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

  /**
   * Find book by ISBN (derived query method) Interview Point: Method name is parsed to generate
   * query
   */
  Optional<Book> findByIsbn(String isbn);

  /**
   * Check if book exists by ISBN Interview Point: Exists queries are optimized to return boolean
   * without loading entity
   */
  boolean existsByIsbn(String isbn);

  /**
   * Find books by author (case-insensitive) Interview Point: IgnoreCase suffix generates UPPER()
   * comparison
   */
  List<Book> findByAuthorIgnoreCase(String author);

  /**
   * Find books by category with pagination Interview Point: Pageable parameter enables pagination
   * and sorting
   */
  Page<Book> findByCategory(String category, Pageable pageable);

  /** Find books by price range Interview Point: Between keyword generates range query */
  List<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

  /**
   * Search books by title or author (custom JPQL query) Interview Point: @Query with JPQL prevents
   * SQL injection through parameter binding
   */
  @Query(
      "SELECT b FROM Book b WHERE "
          + "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Book> searchByTitleOrAuthor(@Param("searchTerm") String searchTerm, Pageable pageable);

  /**
   * Find books with low stock (native SQL query) Interview Point: nativeQuery = true allows
   * database-specific SQL
   */
  @Query(
      value = "SELECT * FROM books WHERE stock_quantity < :threshold ORDER BY stock_quantity ASC",
      nativeQuery = true)
  List<Book> findBooksWithLowStock(@Param("threshold") Integer threshold);

  /** Count books by category Interview Point: Count queries return Long, not entities */
  Long countByCategory(String category);

  /**
   * Find top 10 most expensive books Interview Point: Top/First keywords with OrderBy for limited
   * results
   */
  List<Book> findTop10ByOrderByPriceDesc();
}
