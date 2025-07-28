package com.jena.bookapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 *
 * <p>Interview Points: 1. OpenAPI 3.0 specification for API documentation 2. JWT Bearer token
 * authentication scheme 3. Multiple server environments (dev, staging, prod) 4. Comprehensive API
 * metadata for better developer experience
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Book Management API")
                                .version("1.0.0")
                                .description(
                                        "Production-grade Spring Boot 3.x REST API for book management with comprehensive security and optimizations")
                                .contact(
                                        new Contact()
                                                .name("API Support")
                                                .email("support@bookapi.com")
                                                .url("https://bookapi.com/support"))
                                .license(
                                        new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .servers(
                        List.of(
                                new Server().url("http://localhost:8080").description("Development server"),
                                new Server().url("https://staging-api.bookapi.com").description("Staging server"),
                                new Server().url("https://api.bookapi.com").description("Production server")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("JWT Bearer token authentication")));
    }
}
