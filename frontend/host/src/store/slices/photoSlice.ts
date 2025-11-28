import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { photoApi, PhotoResponse, PhotoStatus } from '../../api/photoApi';

interface PhotoState {
  photos: PhotoResponse[];
  selectedPhoto: PhotoResponse | null;
  loading: boolean;
  error: string | null;
  uploadProgress: { [key: string]: number };
  filters: {
    status: PhotoStatus | null;
    userId: string | null;
  };
}

const initialState: PhotoState = {
  photos: [],
  selectedPhoto: null,
  loading: false,
  error: null,
  uploadProgress: {},
  filters: {
    status: null,
    userId: null,
  },
};

// Async thunks
export const fetchPhotos = createAsyncThunk(
  'photos/fetchPhotos',
  async ({ userId, status, page = 0, size = 20 }: { 
    userId?: string; 
    status?: PhotoStatus; 
    page?: number; 
    size?: number;
  }) => {
    const response = await photoApi.getPhotos(userId, status, page, size);
    return response.data;
  }
);

export const uploadPhoto = createAsyncThunk(
  'photos/uploadPhoto',
  async ({ file, userId }: { file: File; userId: string }, { dispatch }) => {
    const response = await photoApi.uploadPhoto(file, userId, (progress) => {
      dispatch(setUploadProgress({ fileId: file.name, progress }));
    });
    return response.data;
  }
);

export const deletePhoto = createAsyncThunk(
  'photos/deletePhoto',
  async (photoId: number) => {
    await photoApi.deletePhoto(photoId);
    return photoId;
  }
);

export const retryPhoto = createAsyncThunk(
  'photos/retryPhoto',
  async (photoId: number) => {
    await photoApi.retryProcessing(photoId);
    return photoId;
  }
);

const photoSlice = createSlice({
  name: 'photos',
  initialState,
  reducers: {
    setSelectedPhoto: (state, action: PayloadAction<PhotoResponse | null>) => {
      state.selectedPhoto = action.payload;
    },
    setUploadProgress: (state, action: PayloadAction<{ fileId: string; progress: number }>) => {
      state.uploadProgress[action.payload.fileId] = action.payload.progress;
    },
    clearUploadProgress: (state, action: PayloadAction<string>) => {
      delete state.uploadProgress[action.payload];
    },
    setFilters: (state, action: PayloadAction<Partial<PhotoState['filters']>>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch photos
      .addCase(fetchPhotos.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchPhotos.fulfilled, (state, action) => {
        state.loading = false;
        state.photos = action.payload.content;
      })
      .addCase(fetchPhotos.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch photos';
      })
      // Upload photo
      .addCase(uploadPhoto.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(uploadPhoto.fulfilled, (state, action) => {
        state.loading = false;
        state.photos.unshift(action.payload);
      })
      .addCase(uploadPhoto.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to upload photo';
      })
      // Delete photo
      .addCase(deletePhoto.fulfilled, (state, action) => {
        state.photos = state.photos.filter(p => p.id !== action.payload);
        if (state.selectedPhoto?.id === action.payload) {
          state.selectedPhoto = null;
        }
      })
      // Retry photo
      .addCase(retryPhoto.fulfilled, (state, action) => {
        const photo = state.photos.find(p => p.id === action.payload);
        if (photo) {
          photo.status = 'PENDING';
        }
      });
  },
});

export const { 
  setSelectedPhoto, 
  setUploadProgress, 
  clearUploadProgress, 
  setFilters,
  clearError 
} = photoSlice.actions;

export default photoSlice.reducer;

