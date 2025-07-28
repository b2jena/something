package com.jena.bookapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application Class
 *
 * <p>Interview Points: 1. @SpringBootApplication
 * combines @Configuration, @EnableAutoConfiguration, @ComponentScan 2. SpringApplication.run()
 * creates ApplicationContext, registers beans, starts embedded server 3. Auto-configuration works
 * via spring.factories and @Conditional annotations 4. Fat JAR contains all dependencies and
 * embedded Tomcat for standalone deployment
 */
@SpringBootApplication
@EnableCaching // Enables Spring's annotation-driven cache management
@EnableAsync // Enables @Async annotation for asynchronous method execution
@EnableTransactionManagement // Enables @Transactional annotation support
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize/@PostAuthorize
public class BookApiApplication {

  /**
   * Spring Boot Startup Process (Interview Question): 1. SpringApplication.run() creates
   * SpringApplication instance 2. Determines application type (SERVLET, REACTIVE, NONE) 3. Loads
   * ApplicationContextInitializers and ApplicationListeners 4. Creates and configures
   * ApplicationContext 5. Runs auto-configuration classes based on classpath 6. Scans
   * for @Component, @Service, @Repository, @Controller 7. Starts embedded server (Tomcat by
   * default) 8. Publishes ApplicationReadyEvent
   */
  public static void main(String[] args) {
    // JVM optimizations for production
    System.setProperty("spring.jmx.enabled", "true");
    System.setProperty(
        "management.endpoints.web.exposure.include", "health,info,metrics,prometheus");

    SpringApplication.run(BookApiApplication.class, args);
  }
}
