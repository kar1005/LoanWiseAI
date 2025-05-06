import axios from 'axios';
import { store } from '../redux/store';

// Centralized base URL - only change this during deployment
const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add a request interceptor to include auth token in requests
api.interceptors.request.use(
  (config) => {
    const state = store.getState();
    const token = state.auth.token;
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add a response interceptor to handle common errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const { response } = error;
    
    // Handle token expiration or auth errors
    if (response && response.status === 401) {
      // Dispatch logout action if needed
      // store.dispatch(logout());
    }
    
    return Promise.reject(error);
  }
);

export default api;