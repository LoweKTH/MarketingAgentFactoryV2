// src/api/axiosInstance.js
import axios from 'axios';

// Base URL for API calls that require authentication
// Adjust this if your protected endpoints are under a different path than '/api'
const API_BASE_URL = 'http://localhost:8080/api';

// Create an Axios instance for authenticated requests
const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor
axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwtToken');

        // Attach the token to the Authorization header if it exists
        // This interceptor applies to all requests made with axiosInstance
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// You can also add a response interceptor here for global error handling,
// e.g., redirecting to login on 401 Unauthorized errors.
axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        // Example: Handle 401 Unauthorized globally
        if (error.response && error.response.status === 401) {
            console.error('Unauthorized request - Token might be expired or invalid.');
            // Optionally, clear the token and redirect to login
            localStorage.removeItem('jwtToken');
            // window.location.href = '/login'; // Or use your router's navigate method
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;

// Create a separate Axios instance for authentication (login, logout)
// These endpoints typically don't require an Authorization header or get a new token.
export const authAxiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api/auth', // Specific base for auth endpoints
    headers: {
        'Content-Type': 'application/json',
    },
});