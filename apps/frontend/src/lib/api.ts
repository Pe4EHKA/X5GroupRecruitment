import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// Determine the API base URL based on environment
// - Client-side (browser): connect to backend at localhost:8080 directly
// - Server-side (SSR): use API_INTERNAL_URL if set (for Docker: http://backend:8080)
const isServer = typeof window === 'undefined';
const API_BASE_URL = isServer 
  ? (process.env.API_INTERNAL_URL || 'http://localhost:8080')
  : 'http://localhost:8080'; // Browser connects directly to backend

// Create axios instance
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    // Get auth credentials from localStorage
    if (typeof window !== 'undefined') {
      const authCredentials = localStorage.getItem('authCredentials');
      if (authCredentials) {
        config.headers.Authorization = `Basic ${authCredentials}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login on unauthorized
      if (typeof window !== 'undefined') {
        localStorage.removeItem('authCredentials');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Generic API methods
export const api = {
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> =>
    apiClient.get<T>(url, config),

  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> =>
    apiClient.post<T>(url, data, config),

  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> =>
    apiClient.put<T>(url, data, config),

  patch: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> =>
    apiClient.patch<T>(url, data, config),

  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> =>
    apiClient.delete<T>(url, config),
};

export default api;
