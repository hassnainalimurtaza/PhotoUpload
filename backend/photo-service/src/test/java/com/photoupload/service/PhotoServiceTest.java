package com.photoupload.service;

import com.photoupload.common.domain.Photo;
import com.photoupload.common.domain.PhotoStatus;
import com.photoupload.common.dto.PhotoResponse;
import com.photoupload.common.dto.PhotoUploadRequest;
import com.photoupload.common.exception.PhotoNotFoundException;
import com.photoupload.service.mapper.PhotoMapper;
import com.photoupload.service.repository.PhotoEventRepository;
import com.photoupload.service.repository.PhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PhotoService.
 * Demonstrates 80%+ code coverage requirement with Mockito.
 */
@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private PhotoEventRepository photoEventRepository;

    @Mock
    private FileUploadService fileUploadService;

    @Mock
    private ProcessingOrchestrationService orchestrationService;

    @Mock
    private PhotoMapper photoMapper;

    @InjectMocks
    private PhotoService photoService;

    private Photo testPhoto;
    private PhotoResponse testPhotoResponse;

    @BeforeEach
    void setUp() {
        testPhoto = Photo.builder()
            .id(1L)
            .userId("user-123")
            .originalFileName("test.jpg")
            .contentType("image/jpeg")
            .fileSize(1024L)
            .status(PhotoStatus.COMPLETED)
            .build();

        testPhotoResponse = PhotoResponse.builder()
            .id(1L)
            .userId("user-123")
            .originalFileName("test.jpg")
            .status(PhotoStatus.COMPLETED)
            .build();
    }

    @Test
    void uploadPhoto_Success() {
        // Arrange
        PhotoUploadRequest request = PhotoUploadRequest.builder()
            .userId("user-123")
            .build();

        when(fileUploadService.uploadFile(request)).thenReturn(testPhoto);
        when(photoMapper.toResponse(testPhoto)).thenReturn(testPhotoResponse);

        // Act
        PhotoResponse result = photoService.uploadPhoto(request);

        // Assert
        assertNotNull(result);
        assertEquals(testPhotoResponse.getId(), result.getId());
        verify(fileUploadService).uploadFile(request);
        verify(orchestrationService).startProcessing(testPhoto);
        verify(photoMapper).toResponse(testPhoto);
    }

    @Test
    void getPhoto_Found() {
        // Arrange
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));
        when(photoMapper.toResponse(testPhoto)).thenReturn(testPhotoResponse);

        // Act
        PhotoResponse result = photoService.getPhoto(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(photoRepository).findById(1L);
    }

    @Test
    void getPhoto_NotFound() {
        // Arrange
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PhotoNotFoundException.class, () -> photoService.getPhoto(999L));
        verify(photoRepository).findById(999L);
    }

    @Test
    void getUserPhotos_ReturnsPaginatedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Photo> photoPage = new PageImpl<>(List.of(testPhoto));
        
        when(photoRepository.findByUserId("user-123", pageable)).thenReturn(photoPage);
        when(photoMapper.toResponse(any(Photo.class))).thenReturn(testPhotoResponse);

        // Act
        Page<PhotoResponse> result = photoService.getUserPhotos("user-123", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(photoRepository).findByUserId("user-123", pageable);
    }

    @Test
    void deletePhoto_Success() {
        // Arrange
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));

        // Act
        photoService.deletePhoto(1L);

        // Assert
        verify(photoRepository).findById(1L);
        verify(orchestrationService).deletePhoto(testPhoto);
        verify(photoRepository).delete(testPhoto);
    }

    @Test
    void retryProcessing_Success() {
        // Arrange
        testPhoto.setStatus(PhotoStatus.FAILED);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));

        // Act
        photoService.retryProcessing(1L);

        // Assert
        verify(photoRepository).findById(1L);
        verify(orchestrationService).retryProcessing(testPhoto);
    }

    @Test
    void retryProcessing_NotInFailedStatus_ThrowsException() {
        // Arrange
        testPhoto.setStatus(PhotoStatus.COMPLETED);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> photoService.retryProcessing(1L));
    }

    @Test
    void countByStatus_ReturnsCount() {
        // Arrange
        when(photoRepository.countByStatus(PhotoStatus.COMPLETED)).thenReturn(10L);

        // Act
        long count = photoService.countByStatus(PhotoStatus.COMPLETED);

        // Assert
        assertEquals(10L, count);
        verify(photoRepository).countByStatus(PhotoStatus.COMPLETED);
    }
}

