import axios, { AxiosInstance, AxiosProgressEvent } from 'axios';

export type PhotoStatus = 'PENDING' | 'UPLOADING' | 'UPLOADED' | 'PROCESSING' | 'RETRYING' | 'COMPLETED' | 'FAILED';

export interface PhotoResponse {
  id: number;
  userId: string;
  originalFileName: string;
  contentType: string;
  fileSize: number;
  storageUrl?: string;
  thumbnailUrl?: string;
  status: PhotoStatus;
  width?: number;
  height?: number;
  metadata?: string;
  checksum?: string;
  uploadedAt: string;
  processedAt?: string;
  updatedAt: string;
  retryCount?: number;
  lastError?: string;
}

export interface PhotoEventResponse {
  id: number;
  photoId: number;
  eventType: string;
  timestamp: string;
  details?: string;
  userId?: string;
  correlationId?: string;
  sourceService?: string;
  success?: boolean;
  errorMessage?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

class PhotoApi {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        // Add authentication token if available
        const token = localStorage.getItem('auth_token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        // Add basic auth for demo
        config.auth = {
          username: 'user',
          password: 'password',
        };
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response) {
          // Handle specific error codes
          switch (error.response.status) {
            case 401:
              console.error('Unauthorized - please log in');
              break;
            case 403:
              console.error('Forbidden - insufficient permissions');
              break;
            case 404:
              console.error('Resource not found');
              break;
            case 409:
              console.error('Conflict - resource already exists');
              break;
            case 503:
              console.error('Service temporarily unavailable');
              break;
            default:
              console.error('API error:', error.response.data);
          }
        } else if (error.request) {
          console.error('Network error - no response received');
        } else {
          console.error('Error setting up request:', error.message);
        }
        return Promise.reject(error);
      }
    );
  }

  /**
   * Upload a photo
   */
  async uploadPhoto(
    file: File,
    userId: string,
    onProgress?: (progress: number) => void
  ) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId);

    return this.client.post<PhotoResponse>('/photos/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent: AxiosProgressEvent) => {
        if (progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress?.(progress);
        }
      },
    });
  }

  /**
   * Get photos with pagination and filtering
   */
  async getPhotos(
    userId?: string,
    status?: PhotoStatus,
    page: number = 0,
    size: number = 20
  ) {
    return this.client.get<PageResponse<PhotoResponse>>('/photos', {
      params: {
        userId,
        status,
        page,
        size,
        sort: 'uploadedAt,desc',
      },
    });
  }

  /**
   * Get photo by ID
   */
  async getPhoto(id: number) {
    return this.client.get<PhotoResponse>(`/photos/${id}`);
  }

  /**
   * Delete photo
   */
  async deletePhoto(id: number) {
    return this.client.delete(`/photos/${id}`);
  }

  /**
   * Retry failed photo processing
   */
  async retryProcessing(id: number) {
    return this.client.post(`/photos/${id}/retry`);
  }

  /**
   * Get photo events
   */
  async getPhotoEvents(id: number) {
    return this.client.get<PhotoEventResponse[]>(`/photos/${id}/events`);
  }

  /**
   * Get photo statistics
   */
  async getStats() {
    return this.client.get('/photos/stats');
  }
}

export const photoApi = new PhotoApi();

