// src/redux/store.js

import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';

// Configure Redux store
export const store = configureStore({
  reducer: {
    auth: authReducer,
    // Add other reducers here as needed
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore these action types (common non-serializable values)
        ignoredActions: ['auth/register/fulfilled', 'auth/login/fulfilled'],
      },
    }),
});