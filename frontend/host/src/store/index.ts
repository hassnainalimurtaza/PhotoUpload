import { configureStore } from '@reduxjs/toolkit';
import photoReducer from './slices/photoSlice';
import uiReducer from './slices/uiSlice';

export const store = configureStore({
  reducer: {
    photos: photoReducer,
    ui: uiReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore these action types
        ignoredActions: ['photos/uploadPhoto'],
        // Ignore these field paths in all actions
        ignoredActionPaths: ['payload.file'],
        // Ignore these paths in the state
        ignoredPaths: ['photos.uploadingFiles'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

