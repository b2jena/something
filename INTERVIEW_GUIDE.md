# Spring Boot Interview Guide

## 🎯 Core Spring Boot Concepts

### 1. Spring Boot Auto-Configuration

**How it works:**

- Spring Boot scans classpath for dependencies
- Uses `@Conditional` annotations to decide which beans to create
- Configuration classes in `spring.factories` are loaded automatically
- `@EnableAutoConfiguration` triggers this process

**Example:**

```java

@ConditionalOnClass(DataSource.class)
@ConditionalOnMissingBean(DataSource.class)
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {
    // Auto-configures DataSource if conditions are met
}
```

**Interview Questions:**

- Q: How does Spring Boot know which beans to create?
- A: Through auto-configuration classes that use `@Conditional` annotations to check classpath and existing beans.

### 2. Spring Application Startup Process

**Detailed Steps:**

1. `SpringApplication.run()` creates SpringApplication instance
2. Determines web application type (SERVLET/REACTIVE/NONE)
3. Loads `ApplicationContextInitializers` from `spring.factories`
4. Loads `ApplicationListeners` from `spring.factories`
5. Deduces main application class from stack trace
6. Creates appropriate `ApplicationContext` (AnnotationConfigServletWebServerApplicationContext)
7. Prepares context with environment and profiles
8. Runs `ApplicationContextInitializers`
9. Publishes `ApplicationStartingEvent`
10. Loads configuration classes and component scanning
11. Runs auto-configuration classes
12. Creates and starts embedded web server
13. Publishes `ApplicationReadyEvent`

**Interview Questions:**

- Q: What happens during SpringApplication.run()?
- A: Creates ApplicationContext, runs auto-configuration, starts embedded server, and publishes lifecycle events.

### 3. Bean Lifecycle and Scopes

**Bean Scopes:**

- **Singleton** (default): One instance per Spring container
- **Prototype**: New instance for each request
- **Request**: One instance per HTTP request (web apps)
- **Session**: One instance per HTTP session (web apps)
- **Application**: One instance per ServletContext (web apps)

**Lifecycle Phases:**

```java

@Component
public class MyBean implements InitializingBean, DisposableBean {

    @PostConstruct
    public void init() {
        // Called after dependency injection
    }

    @Override
    public void afterPropertiesSet() {
        // InitializingBean callback
    }

    @PreDestroy
    public void cleanup() {
        // Called before bean destruction
    }

    @Override
    public void destroy() {
        // DisposableBean callback
    }
}
```

**Interview Questions:**

- Q: What's the difference between @PostConstruct and InitializingBean?
- A: @PostConstruct is JSR-250 standard, InitializingBean is Spring-specific. Both called after dependency injection.

### 4. Dependency Injection Types

**Constructor Injection (Recommended):**

```java

@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }
}
```

**Field Injection (Not Recommended):**

```java

@Service
public class BookService {
    @Autowired
    private BookRepository repository;
}
```

**Setter Injection:**

```java

@Service
public class BookService {
    private BookRepository repository;

    @Autowired
    public void setRepository(BookRepository repository) {
        this.repository = repository;
    }
}
```

**Interview Questions:**

- Q: Why is constructor injection preferred?
- A: Ensures immutability, makes dependencies explicit, enables fail-fast behavior, and works without Spring container.

## 🔒 Spring Security Deep Dive

### 1. Security Filter Chain

**Filter Order:**

1. `SecurityContextPersistenceFilter`
2. `UsernamePasswordAuthenticationFilter`
3. `JwtAuthenticationFilter` (custom)
4. `FilterSecurityInterceptor`

**Configuration:**

```java

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/books/**").hasRole("USER")
                    .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                    .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}
```

### 2. JWT Implementation

**Token Structure:**

- **Header**: Algorithm and token type
- **Payload**: Claims (user info, roles, expiration)
- **Signature**: Verification hash

**Security Considerations:**

- Use strong secret keys (256-bit minimum)
- Set appropriate expiration times
- Validate signature and expiration
- Store sensitive data server-side, not in JWT

**Interview Questions:**

- Q: How do you secure JWT tokens?
- A: Use HTTPS, strong secrets, short expiration, validate signature, and don't store sensitive data in payload.

### 3. Method-Level Security

```java

@Service
public class BookService {

    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public BookResponse createBook(BookRequest request) {
        // Only ADMIN or LIBRARIAN can create books
    }

    @PostAuthorize("returnObject.author == authentication.name or hasRole('ADMIN')")
    public BookResponse getBook(Long id) {
        // Users can only see their own books unless they're admin
    }
}
```

## 💾 Data Access and JPA

### 1. JPA Entity Lifecycle

**States:**

- **NEW**: Entity created but not persisted
- **MANAGED**: Entity associated with persistence context
- **DETACHED**: Entity was managed but persistence context closed
- **REMOVED**: Entity marked for deletion

**Hibernate Caching:**

- **L1 Cache**: Session-scoped (automatic)
- **L2 Cache**: SessionFactory-scoped (optional)
- **Query Cache**: Caches query results

### 2. Repository Pattern

**Spring Data JPA Query Methods:**

```java
public interface BookRepository extends JpaRepository<Book, Long> {

    // Derived query methods
    List<Book> findByAuthorIgnoreCase(String author);

    Page<Book> findByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);

    // Custom JPQL
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:title%")
    List<Book> findByTitleContaining(@Param("title") String title);

    // Native SQL
    @Query(value = "SELECT * FROM books WHERE stock_quantity < ?1", nativeQuery = true)
    List<Book> findLowStockBooks(Integer threshold);
}
```

**Interview Questions:**

- Q: What's the difference between JpaRepository and CrudRepository?
- A: JpaRepository extends PagingAndSortingRepository which extends CrudRepository, adding batch operations and
  pagination.

### 3. Transaction Management

**Propagation Types:**

- **REQUIRED** (default): Join existing or create new
- **REQUIRES_NEW**: Always create new transaction
- **SUPPORTS**: Join if exists, run without if not
- **NOT_SUPPORTED**: Run without transaction
- **NEVER**: Throw exception if transaction exists
- **MANDATORY**: Throw exception if no transaction

**Isolation Levels:**

- **READ_UNCOMMITTED**: Dirty reads possible
- **READ_COMMITTED**: Prevents dirty reads
- **REPEATABLE_READ**: Prevents dirty and non-repeatable reads
- **SERIALIZABLE**: Prevents all phenomena

```java

@Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class,
        timeout = 30
)
public void complexBusinessOperation() {
    // Transaction configuration
}
```

## 🚀 Performance Optimization

### 1. Caching Strategies

**Cache Patterns:**

- **Cache-Aside**: Application manages cache
- **Write-Through**: Write to cache and database simultaneously
- **Write-Behind**: Write to cache immediately, database later
- **Refresh-Ahead**: Proactively refresh cache before expiration

**Spring Cache Annotations:**

```java

@Cacheable(value = "books", key = "#id")
public BookResponse getBook(Long id) {
    // Result cached with key "books::id"
}

@CacheEvict(value = "books", key = "#id")
public void deleteBook(Long id) {
    // Removes cache entry
}

@CachePut(value = "books", key = "#result.id")
public BookResponse updateBook(BookRequest request) {
    // Always executes method and updates cache
}
```

### 2. Database Optimization

**Connection Pooling (HikariCP):**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      leak-detection-threshold: 60000
```

**JPA Performance:**

```java

@Entity
public class Book {
    @BatchSize(size = 10) // Batch fetching
    @OneToMany(fetch = FetchType.LAZY) // Lazy loading
    private List<Review> reviews;
}

// Query optimization
@Query("SELECT b FROM Book b JOIN FETCH b.reviews WHERE b.id = :id")
Book findBookWithReviews(@Param("id") Long id);
```

### 3. Async Processing

```java

@Async("taskExecutor")
@PreAuthorize("hasRole('ADMIN')")
public CompletableFuture<List<BookResponse>> processLargeDataset() {
    // Non-blocking operation
    return CompletableFuture.completedFuture(results);
}
```

## 🧪 Testing Strategies

### 1. Test Types

**Unit Tests:**

```java

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookService service;

    @Test
    void shouldCreateBook() {
        // Test with mocked dependencies
    }
}
```

**Integration Tests:**

```java

@SpringBootTest
@Testcontainers
class BookApiIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void shouldCreateBookEndToEnd() {
        // Test with real database
    }
}
```

**Web Layer Tests:**

```java

@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService service;

    @Test
    void shouldReturnBooks() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk());
    }
}
```

### 2. Mock vs MockBean

**@Mock (Mockito):**

- Pure unit testing
- Faster execution
- No Spring context

**@MockBean (Spring Boot):**

- Integration testing
- Replaces beans in Spring context
- Slower but more realistic

**Interview Questions:**

- Q: When would you use @MockBean vs @Mock?
- A: Use @Mock for pure unit tests, @MockBean when you need Spring context but want to mock specific beans.

## 🏗️ Architecture Patterns

### 1. Layered Architecture

```
Controller Layer    -> REST endpoints, validation
Service Layer      -> Business logic, transactions
Repository Layer   -> Data access, queries
Entity Layer       -> Domain models
```

### 2. HATEOAS Implementation

```java

@GetMapping("/{id}")
public EntityModel<BookResponse> getBook(@PathVariable Long id) {
    BookResponse book = bookService.getBook(id);

    return EntityModel.of(book)
            .add(linkTo(methodOn(BookController.class).getBook(id)).withSelfRel())
            .add(linkTo(BookController.class).withRel("books"))
            .add(linkTo(methodOn(BookController.class).updateBook(id, null)).withRel("update"));
}
```

### 3. Exception Handling

```java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail handleBookNotFound(BookNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // Handle validation errors
    }
}
```

## 🔧 Configuration Management

### 1. Profiles

```yaml
# application.yml
spring:
  profiles:
    active: dev

---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:testdb

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/bookapi
```

### 2. Configuration Properties

```java

@ConfigurationProperties(prefix = "app.jwt")
@Component
public class JwtProperties {
    private String secret;
    private long expiration;

    // getters and setters
}
```

## 📊 Monitoring and Observability

### 1. Actuator Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### 2. Custom Metrics

```java

@Component
public class BookMetrics {
    private final Counter bookCreatedCounter;
    private final Timer bookSearchTimer;

    public BookMetrics(MeterRegistry meterRegistry) {
        this.bookCreatedCounter = Counter.builder("books.created")
                .description("Number of books created")
                .register(meterRegistry);

        this.bookSearchTimer = Timer.builder("books.search.duration")
                .description("Book search duration")
                .register(meterRegistry);
    }
}
```

## 🐳 Containerization

### 1. Docker Best Practices

```dockerfile
# Multi-stage build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM gcr.io/distroless/java17-debian11:nonroot
USER nonroot:nonroot
COPY --from=builder --chown=nonroot:nonroot /app/target/*.jar /app/app.jar

# JVM tuning
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 2. Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: book-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: book-api
  template:
    metadata:
      labels:
        app: book-api
    spec:
      containers:
        - name: book-api
          image: book-api:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
```

## 🎯 Common Interview Questions

### Spring Boot Specific

1. **Q: What is Spring Boot and how is it different from Spring Framework?**
   A: Spring Boot is an opinionated framework that provides auto-configuration, embedded servers, and production-ready
   features on top of Spring Framework.

2. **Q: Explain Spring Boot auto-configuration.**
   A: Auto-configuration automatically configures Spring application based on classpath dependencies using @Conditional
   annotations.

3. **Q: What is the difference between @Component, @Service, and @Repository?**
   A: All are stereotypes of @Component. @Service indicates business logic, @Repository indicates data access layer with
   exception translation.

4. **Q: How does Spring Boot handle externalized configuration?**
   A: Through application.properties/yml, environment variables, command line arguments, and @ConfigurationProperties.

### Security

5. **Q: How do you implement JWT authentication in Spring Boot?**
   A: Create JWT filter, configure SecurityFilterChain, validate tokens, and set SecurityContext.

6. **Q: What is CSRF and how does Spring Security handle it?**
   A: Cross-Site Request Forgery. Spring Security provides CSRF tokens for state-changing operations.

### Data Access

7. **Q: What is the difference between JPA and Hibernate?**
   A: JPA is specification, Hibernate is implementation. JPA provides standard API, Hibernate adds additional features.

8. **Q: Explain different types of JPA relationships.**
   A: @OneToOne, @OneToMany, @ManyToOne, @ManyToMany with different fetch types and cascade options.

### Performance

9. **Q: How do you optimize Spring Boot application performance?**
   A: Connection pooling, caching, async processing, database indexing, JVM tuning, and monitoring.

10. **Q: What caching strategies do you know?**
    A: Cache-aside, write-through, write-behind, refresh-ahead with different eviction policies.

This guide covers the essential concepts demonstrated in the production-grade Spring Boot application, providing both
theoretical knowledge and practical implementation details for interview preparation.