# Production-Grade Spring Boot 3.x REST API

A comprehensive, production-ready Spring Boot 3.x REST API demonstrating modern Java development practices, security
hardening, and performance optimizations for interview preparation.

## 🚀 Features

### Core Technologies

- **Spring Boot 3.5.4** with Java 17
- **Spring Security 6.x** with JWT authentication
- **Spring Data JPA** with Hibernate
- **Redis** for distributed caching
- **PostgreSQL** for production database
- **MapStruct** for efficient mapping
- **OpenAPI 3.0** for documentation

### Security Hardening

- JWT-based authentication with role-based access control
- CSRF protection and CORS configuration
- Security headers (CSP, HSTS, X-Frame-Options)
- Input validation and sanitization
- OWASP dependency scanning
- Secure password encoding with BCrypt

### Performance Optimizations

- Redis distributed caching with TTL strategies
- HikariCP connection pooling
- Async operations with custom thread pools
- Database indexing and query optimization
- JVM tuning for containerized environments

### Production Features

- Comprehensive logging with MDC tracing
- Actuator endpoints for monitoring
- Prometheus metrics integration
- Global exception handling
- HATEOAS hypermedia support
- Docker containerization
- CI/CD pipeline with GitHub Actions

## 📋 Interview Topics Covered

### Spring Boot Internals

- **Auto-configuration**: How Spring Boot configures beans automatically
- **Application startup**: SpringApplication.run() lifecycle
- **Bean lifecycle**: Creation, initialization, and destruction
- **Dependency injection**: Constructor vs field injection
- **Profiles**: Environment-specific configurations

### Spring Framework Core

- **IoC Container**: BeanFactory vs ApplicationContext
- **AOP**: Aspect-oriented programming with proxies
- **Transaction management**: @Transactional propagation and isolation
- **Event handling**: ApplicationEvent and listeners
- **Scopes**: Singleton, prototype, request, session

### Data Access

- **JPA/Hibernate**: Entity lifecycle, lazy loading, caching
- **Repository pattern**: Spring Data JPA query methods
- **Transaction boundaries**: Service layer transactions
- **Connection pooling**: HikariCP configuration
- **Database migrations**: Flyway/Liquibase integration

### Security

- **Authentication vs Authorization**: Concepts and implementation
- **JWT tokens**: Structure, validation, and security
- **Method-level security**: @PreAuthorize/@PostAuthorize
- **CORS**: Cross-origin resource sharing
- **CSRF**: Cross-site request forgery protection

### Testing

- **Unit testing**: Mockito vs @MockBean
- **Integration testing**: @SpringBootTest vs @WebMvcTest
- **Testcontainers**: Real database testing
- **Test slices**: Focused testing with specific contexts

## 🏗️ Architecture

```
├── controller/     # REST endpoints with HATEOAS
├── service/        # Business logic layer
├── repository/     # Data access layer
├── entity/         # JPA entities
├── dto/            # Data transfer objects (Records)
├── mapper/         # MapStruct mappers
├── config/         # Configuration classes
├── security/       # Security components
└── exception/      # Custom exceptions and handlers
```

## 🚦 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Redis (for caching)
- PostgreSQL (for production)

### Running Locally

1. **Clone the repository**

```bash
git clone <repository-url>
cd book-api
```

2. **Start dependencies**

```bash
docker-compose up -d redis postgres
```

3. **Run the application**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

4. **Access the API**

- API: http://localhost:8080/api/v1/books
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (dev profile)
- Actuator: http://localhost:8080/actuator

### Running Tests

```bash
# Unit tests
./mvnw test

# Integration tests with Testcontainers
./mvnw verify

# Security scan
./mvnw org.owasp:dependency-check-maven:check
```

## 🔐 Authentication

The API uses JWT Bearer tokens for authentication. Include the token in the Authorization header:

```bash
curl -H "Authorization: Bearer <jwt-token>" \
     http://localhost:8080/api/v1/books
```

### Roles

- **USER**: Read access to books
- **LIBRARIAN**: Create and update books
- **ADMIN**: Full access including delete operations

## 📊 API Endpoints

| Method | Endpoint                                | Description                 | Roles      |
|--------|-----------------------------------------|-----------------------------|------------|
| GET    | `/api/v1/books`                         | Get all books (paginated)   | USER+      |
| GET    | `/api/v1/books/{id}`                    | Get book by ID              | USER+      |
| POST   | `/api/v1/books`                         | Create new book             | LIBRARIAN+ |
| PUT    | `/api/v1/books/{id}`                    | Update book                 | LIBRARIAN+ |
| DELETE | `/api/v1/books/{id}`                    | Delete book                 | ADMIN      |
| GET    | `/api/v1/books/search?q={term}`         | Search books                | USER+      |
| GET    | `/api/v1/books/category/{category}`     | Get books by category       | USER+      |
| GET    | `/api/v1/books/low-stock?threshold={n}` | Get low stock books (async) | ADMIN      |

## 🐳 Docker Deployment

### Build and run with Docker

```bash
# Build image
docker build -t book-api:latest .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=postgres \
  -e REDIS_HOST=redis \
  book-api:latest
```

### Docker Compose

```bash
docker-compose up -d
```

## 📈 Monitoring

### Actuator Endpoints

- `/actuator/health` - Application health
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/info` - Application information

### Logging

- Structured logging with JSON format
- MDC for request tracing
- Log levels configurable per environment
- Log rotation and archival

## 🔧 Configuration

### Environment Variables

| Variable                 | Description        | Default     |
|--------------------------|--------------------|-------------|
| `SPRING_PROFILES_ACTIVE` | Active profile     | `dev`       |
| `DB_HOST`                | Database host      | `localhost` |
| `DB_PORT`                | Database port      | `5432`      |
| `DB_NAME`                | Database name      | `bookapi`   |
| `DB_USERNAME`            | Database username  | `bookapi`   |
| `DB_PASSWORD`            | Database password  | `password`  |
| `REDIS_HOST`             | Redis host         | `localhost` |
| `REDIS_PORT`             | Redis port         | `6379`      |
| `JWT_SECRET`             | JWT signing secret | (generated) |

## 🧪 Testing Strategy

### Unit Tests

- Service layer with Mockito
- Repository layer with @DataJpaTest
- Controller layer with @WebMvcTest
- Security configuration testing

### Integration Tests

- Full application context with @SpringBootTest
- Real databases with Testcontainers
- End-to-end API testing
- Security integration testing

### Performance Tests

- Load testing with JMeter
- Database performance profiling
- Cache hit ratio monitoring
- Memory usage analysis

## 🚀 CI/CD Pipeline

The project includes a comprehensive GitHub Actions pipeline:

1. **Test Stage**
    - Unit and integration tests
    - Code coverage reporting
    - OWASP dependency scanning
    - SonarQube analysis

2. **Build Stage**
    - Docker image building
    - Multi-architecture support
    - Vulnerability scanning with Trivy
    - Image signing and attestation

3. **Deploy Stage**
    - Kubernetes deployment
    - Smoke testing
    - Rollback capabilities
    - Notification integration

## 📚 Interview Questions & Answers

### Spring Boot Startup Process

**Q: Explain how Spring Boot application starts up.**

**A:** Spring Boot startup follows these steps:

1. `SpringApplication.run()` creates a SpringApplication instance
2. Determines application type (SERVLET, REACTIVE, NONE)
3. Loads `ApplicationContextInitializers` and `ApplicationListeners`
4. Creates and configures `ApplicationContext`
5. Runs auto-configuration classes based on classpath scanning
6. Scans for components (`@Component`, `@Service`, `@Repository`, `@Controller`)
7. Starts embedded server (Tomcat by default)
8. Publishes `ApplicationReadyEvent`

### Bean Lifecycle

**Q: Describe Spring Bean lifecycle.**

**A:** Bean lifecycle phases:

1. **Instantiation**: Constructor called
2. **Dependency Injection**: Properties and dependencies set
3. **BeanNameAware**: `setBeanName()` called if implemented
4. **ApplicationContextAware**: `setApplicationContext()` called if implemented
5. **BeanPostProcessor**: `postProcessBeforeInitialization()` called
6. **InitializingBean**: `afterPropertiesSet()` called if implemented
7. **@PostConstruct**: Custom init method called
8. **BeanPostProcessor**: `postProcessAfterInitialization()` called
9. **Bean Ready**: Bean is ready for use
10. **DisposableBean**: `destroy()` called during shutdown if implemented
11. **@PreDestroy**: Custom destroy method called

### Transaction Management

**Q: How does @Transactional work?**

**A:** `@Transactional` works through AOP proxies:

1. Spring creates a proxy around the bean
2. Proxy intercepts method calls
3. Begins transaction before method execution
4. Commits transaction on successful completion
5. Rolls back on unchecked exceptions
6. Supports propagation (REQUIRED, REQUIRES_NEW, etc.)
7. Supports isolation levels (READ_COMMITTED, SERIALIZABLE, etc.)

### Caching Strategy

**Q: Explain the caching implementation.**

**A:** Multi-level caching strategy:

1. **L1 Cache**: Hibernate first-level cache (session-scoped)
2. **L2 Cache**: Hibernate second-level cache (application-scoped)
3. **Application Cache**: Spring Cache with Redis
4. **HTTP Cache**: Browser/CDN caching with proper headers
5. **Cache-aside pattern**: Application manages cache explicitly
6. **TTL strategies**: Different expiration times based on data volatility

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the full test suite
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Spring Security team for comprehensive security features
- Testcontainers for integration testing capabilities
- OWASP for security best practices
- The open-source community for continuous improvements