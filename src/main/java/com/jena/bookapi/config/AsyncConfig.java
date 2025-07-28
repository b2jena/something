package com.jena.bookapi.config;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async Configuration for non-blocking operations
 *
 * <p>Interview Points: 1. @EnableAsync enables @Async annotation processing 2. Custom thread pool
 * prevents blocking main application threads 3. Proper thread pool sizing based on CPU cores and
 * I/O operations 4. Exception handling for async operations
 */
@Configuration
@EnableAsync
public class AsyncConfig {

  private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

  /**
   * Custom task executor for async operations Interview Point: ThreadPoolTaskExecutor provides
   * better control than default SimpleAsyncTaskExecutor
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // Core pool size: minimum number of threads
    executor.setCorePoolSize(2);

    // Max pool size: maximum number of threads
    executor.setMaxPoolSize(10);

    // Queue capacity: tasks waiting for execution
    executor.setQueueCapacity(100);

    // Thread name prefix for debugging
    executor.setThreadNamePrefix("BookApi-Async-");

    // Keep alive time for idle threads
    executor.setKeepAliveSeconds(60);

    // Reject policy when queue is full
    executor.setRejectedExecutionHandler(
        (runnable, executor1) -> {
          logger.warn("Async task rejected: {}", runnable.toString());
          throw new RuntimeException("Async task queue is full");
        });

    // Wait for tasks to complete on shutdown
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);

    executor.initialize();

    logger.info(
        "Async task executor initialized with core={}, max={}, queue={}",
        executor.getCorePoolSize(),
        executor.getMaxPoolSize(),
        executor.getQueueCapacity());

    return executor;
  }
}
