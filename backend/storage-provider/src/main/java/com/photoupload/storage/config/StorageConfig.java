package com.photoupload.storage.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Configuration for cloud storage providers.
 * Demonstrates Dependency Inversion Principle: High-level modules depend on abstractions.
 */
@Slf4j
@Configuration
public class StorageConfig {

    /**
     * AWS S3 Client Configuration
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3", matchIfMissing = true)
    public S3Client s3Client(
        @Value("${aws.access-key-id:}") String accessKeyId,
        @Value("${aws.secret-access-key:}") String secretAccessKey,
        @Value("${aws.region:us-east-1}") String region
    ) {
        log.info("Configuring AWS S3 client for region: {}", region);

        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            log.warn("AWS credentials not provided, using default credential provider chain");
            return S3Client.builder()
                .region(Region.of(region))
                .build();
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }

    /**
     * S3 Presigner for generating presigned URLs
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3", matchIfMissing = true)
    public S3Presigner s3Presigner(
        @Value("${aws.region:us-east-1}") String region
    ) {
        return S3Presigner.builder()
            .region(Region.of(region))
            .build();
    }

    /**
     * Google Cloud Storage Configuration
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "gcs")
    public Storage googleCloudStorage(
        @Value("${gcp.project-id}") String projectId
    ) {
        log.info("Configuring Google Cloud Storage for project: {}", projectId);

        return StorageOptions.newBuilder()
            .setProjectId(projectId)
            .build()
            .getService();
    }

    /**
     * Azure Blob Storage Configuration
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "azure")
    public BlobServiceClient azureBlobServiceClient(
        @Value("${azure.storage.connection-string}") String connectionString
    ) {
        log.info("Configuring Azure Blob Storage");

        return new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
    }
}

