package com.photoupload.storage.impl;

import com.photoupload.common.exception.StorageException;
import com.photoupload.storage.CloudStorageProvider;
import com.photoupload.storage.StorageMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AWS S3 implementation of CloudStorageProvider.
 * Implements Strategy pattern for S3-specific operations.
 * Adapter pattern: Adapts S3Client to CloudStorageProvider interface.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3", matchIfMissing = true)
public class S3StorageProvider implements CloudStorageProvider {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String region;

    public S3StorageProvider(
        S3Client s3Client,
        S3Presigner s3Presigner,
        @Value("${aws.s3.bucket-name}") String bucketName,
        @Value("${aws.region:us-east-1}") String region
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.region = region;
        log.info("Initialized S3StorageProvider with bucket: {}, region: {}", bucketName, region);
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType, long fileSize) {
        try {
            log.debug("Uploading file to S3: key={}, contentType={}, size={}", key, contentType, fileSize);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

            PutObjectResponse response = s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(inputStream, fileSize)
            );

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            log.info("Successfully uploaded file to S3: key={}, etag={}", key, response.eTag());

            return url;
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: key={}", key, e);
            throw new StorageException("S3", "upload", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading file to S3: key={}", key, e);
            throw new StorageException("S3", "upload", "Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String key) {
        try {
            log.debug("Downloading file from S3: key={}", key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            return s3Client.getObject(getObjectRequest);
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: key={}", key);
            throw new StorageException("S3", "download", "File not found: " + key, e);
        } catch (S3Exception e) {
            log.error("Failed to download file from S3: key={}", key, e);
            throw new StorageException("S3", "download", e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            log.debug("Deleting file from S3: key={}", key);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from S3: key={}", key);

            return true;
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: key={}", key, e);
            throw new StorageException("S3", "delete", e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking if file exists in S3: key={}", key, e);
            throw new StorageException("S3", "exists", e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String key, Duration duration) {
        try {
            log.debug("Generating presigned URL for S3 object: key={}, duration={}", key, duration);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            log.debug("Generated presigned URL: key={}", key);
            return url;
        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL: key={}", key, e);
            throw new StorageException("S3", "generatePresignedUrl", e.getMessage(), e);
        }
    }

    @Override
    public StorageMetadata getMetadata(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);

            Map<String, String> userMetadata = new HashMap<>(response.metadata());

            return StorageMetadata.builder()
                .key(key)
                .contentType(response.contentType())
                .contentLength(response.contentLength())
                .etag(response.eTag())
                .lastModified(response.lastModified())
                .userMetadata(userMetadata)
                .storageClass(response.storageClassAsString())
                .encryption(response.serverSideEncryptionAsString())
                .build();
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: key={}", key);
            throw new StorageException("S3", "getMetadata", "File not found: " + key, e);
        } catch (S3Exception e) {
            log.error("Failed to get metadata from S3: key={}", key, e);
            throw new StorageException("S3", "getMetadata", e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "S3";
    }
}

