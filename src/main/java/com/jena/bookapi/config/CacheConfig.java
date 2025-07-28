package com.jena.bookapi.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 *
 * <p>Interview Points: 1. Redis provides distributed caching for scalability 2. Different TTL for
 * different cache types based on data volatility 3. JSON serialization for human-readable cached
 * data 4. Cache-aside pattern: application manages cache explicitly
 */
@Configuration
@EnableCaching
@Profile("!test")
public class CacheConfig {

    /**
     * Redis Cache Manager with custom configurations Interview Point: CacheManager is the main
     * abstraction for cache operations
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30)) // Default TTL: 30 minutes
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()))
                        .disableCachingNullValues(); // Don't cache null values

        // Custom configurations for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Books cache - longer TTL as book data changes less frequently
        cacheConfigurations.put("books", defaultConfig.entryTtl(Duration.ofHours(2)));

        // Individual book cache - medium TTL
        cacheConfigurations.put("book", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        // Category-based cache - shorter TTL as it might change more often
        cacheConfigurations.put("booksByCategory", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
