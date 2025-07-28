package com.jena.bookapi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.GET, "/api/v1/books/**")
                                        .hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/api/v1/books")
                                        .hasAnyRole("LIBRARIAN", "ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/**")
                                        .hasAnyRole("LIBRARIAN", "ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**")
                                        .hasRole("ADMIN")
                                        .anyRequest()
                                        .permitAll())
                .build();
    }
}
