package com.jena.bookapi.config;

import com.jena.bookapi.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security Configuration with comprehensive hardening
 *
 * <p>Interview Points: 1. SecurityFilterChain replaces WebSecurityConfigurerAdapter (deprecated) 2.
 * Stateless session management for REST APIs 3. CORS configuration for cross-origin requests 4.
 * Security headers for XSS, CSRF, and clickjacking protection 5. JWT filter positioned before
 * UsernamePasswordAuthenticationFilter
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Main security filter chain Interview Point: Method-based configuration is more flexible than
     * XML
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session management
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // Public endpoints
                                        .requestMatchers("/actuator/health", "/actuator/info")
                                        .permitAll()
                                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                                        .permitAll()
                                        .requestMatchers("/error")
                                        .permitAll()

                                        // API endpoints with role-based access
                                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**")
                                        .hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/api/v1/books")
                                        .hasAnyRole("LIBRARIAN", "ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/**")
                                        .hasAnyRole("LIBRARIAN", "ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**")
                                        .hasRole("ADMIN")

                                        // Actuator endpoints (admin only)
                                        .requestMatchers("/actuator/**")
                                        .hasRole("ADMIN")

                                        // All other requests require authentication
                                        .anyRequest()
                                        .authenticated())

                // Security headers
                .headers(
                        headers ->
                                headers
                                        .httpStrictTransportSecurity(
                                                hstsConfig ->
                                                        hstsConfig
                                                                .maxAgeInSeconds(31536000) // 1 year
                                                                .includeSubDomains(true)
                                                                .preload(true))
                                        .addHeaderWriter(
                                                (request, response) -> {
                                                    // Referrer Policy
                                                    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                                                    // Content Security Policy
                                                    response.setHeader(
                                                            "Content-Security-Policy",
                                                            "default-src 'self'; "
                                                                    + "script-src 'self' 'unsafe-inline'; "
                                                                    + "style-src 'self' 'unsafe-inline'; "
                                                                    + "img-src 'self' data:; "
                                                                    + "font-src 'self'; "
                                                                    + "connect-src 'self'; "
                                                                    + "frame-ancestors 'none'");

                                                    // Additional security headers
                                                    response.setHeader("X-Content-Type-Options", "nosniff");
                                                    response.setHeader("X-Frame-Options", "DENY");
                                                    response.setHeader("X-XSS-Protection", "1; mode=block");
                                                    response.setHeader(
                                                            "Permissions-Policy", "geolocation=(), microphone=(), camera=()");
                                                }))

                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * CORS configuration Interview Point: Restrictive CORS policy prevents unauthorized cross-origin
     * requests
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (configure for production)
        configuration.setAllowedOriginPatterns(
                List.of("http://localhost:*", "https://*.yourdomain.com"));

        // Allow specific methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow specific headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Cache preflight response
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    /**
     * Password encoder bean Interview Point: BCrypt is adaptive and includes salt automatically
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 for better security
    }
}
