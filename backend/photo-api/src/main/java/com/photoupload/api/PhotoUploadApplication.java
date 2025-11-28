package com.photoupload.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for Photo Upload System.
 * Production-grade distributed photo upload and management system.
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.photoupload.api",
    "com.photoupload.service",
    "com.photoupload.storage",
    "com.photoupload.eventbus"
})
@EntityScan(basePackages = "com.photoupload.common.domain")
@EnableJpaRepositories(basePackages = "com.photoupload.service.repository")
public class PhotoUploadApplication {

    public static void main(String[] args) {
        log.info("Starting Photo Upload System...");
        SpringApplication.run(PhotoUploadApplication.class, args);
        log.info("Photo Upload System started successfully");
    }
}

