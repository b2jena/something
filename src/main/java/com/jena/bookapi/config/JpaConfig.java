package com.jena.bookapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 *
 * <p>Interview Points: 1. @EnableJpaAuditing enables automatic auditing (CreatedDate,
 * LastModifiedDate) 2. @EnableJpaRepositories enables Spring Data JPA repository scanning
 * 3. @EnableTransactionManagement enables declarative transaction management 4. Auditing requires
 * AuditorAware bean for user tracking
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.jena.bookapi.repository")
@EnableTransactionManagement
public class JpaConfig {

  // Additional JPA configuration can be added here
  // For example: custom AuditorAware implementation for user tracking
}
