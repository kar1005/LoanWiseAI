// src/services/authService.js

import api from './baseService';

const AUTH_ENDPOINT = '/auth';

// Service for handling auth-related API calls
const authService = {
  // Register a new user
  register: async (userData) => {
    try {
      const response = await api.post(`${AUTH_ENDPOINT}/register`, userData);
      return response.data;
    } catch (error) {
      throw error.response ? error.response.data : new Error('Registration failed');
    }
  },

  // Login user
  login: async (credentials) => {
    try {
      const response = await api.post(`${AUTH_ENDPOINT}/login`, credentials);
      return response.data;
    } catch (error) {
      throw error.response ? error.response.data : new Error('Login failed');
    }
  },

  // Get current user profile
  getProfile: async () => {
    try {
      const response = await api.get(`${AUTH_ENDPOINT}/profile`);
      return response.data;
    } catch (error) {
      throw error.response ? error.response.data : new Error('Failed to fetch profile');
    }
  },

  // Logout (client-side only - no API call needed as JWT is stateless)
  logout: () => {
    // This is handled in the Redux action
    return true;
  }
};

export default authService;