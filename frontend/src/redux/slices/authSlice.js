// src/redux/slices/authSlice.js

import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import authService from '../../services/authService';

// Load auth data from localStorage if available
const token = localStorage.getItem('LoanWisetoken');
const user = localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null;

// Async thunk for user registration
export const register = createAsyncThunk(
  'auth/register',
  async (userData, { rejectWithValue }) => {
    try {
      const response = await authService.register(userData);
      
      // Store auth data in localStorage
      localStorage.setItem('LoanWisetoken', response.token);
      localStorage.setItem('user', JSON.stringify(response.user));
      
      return response;
    } catch (error) {
      return rejectWithValue(error.message || 'Registration failed');
    }
  }
);

// Async thunk for user login
export const login = createAsyncThunk(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await authService.login(credentials);
      
      // Store auth data in localStorage
      localStorage.setItem('LoanWisetoken', response.token);
      localStorage.setItem('user', JSON.stringify(response.user));
      
      return response;
    } catch (error) {
      return rejectWithValue(error.message || 'Login failed');
    }
  }
);

// Async thunk for fetching user profile
export const getProfile = createAsyncThunk(
  'auth/getProfile',
  async (_, { getState, rejectWithValue }) => {
    try {
      const response = await authService.getProfile();
      return response;
    } catch (error) {
      return rejectWithValue(error.message || 'Failed to fetch profile');
    }
  }
);

// Auth slice
const authSlice = createSlice({
  name: 'auth',
  initialState: {
    token: token || null,
    user: user || null,
    isAuthenticated: !!token,
    loading: false,
    error: null,
  },
  reducers: {
    logout: (state) => {
      // Remove auth data from localStorage
      localStorage.removeItem('LoanWisetoken');
      localStorage.removeItem('user');
      
      // Reset state
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      state.error = null;
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Register cases
      .addCase(register.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(register.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.token = action.payload.token;
        state.user = action.payload.user;
      })
      .addCase(register.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Login cases
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.token = action.payload.token;
        state.user = action.payload.user;
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Get profile cases
      .addCase(getProfile.pending, (state) => {
        state.loading = true;
      })
      .addCase(getProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
      })
      .addCase(getProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

// Export actions and reducer
export const { logout, clearError } = authSlice.actions;
export default authSlice.reducer;