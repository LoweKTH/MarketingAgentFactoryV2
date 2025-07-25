// src/api/authApi.js
import axiosInstance, { authAxiosInstance } from './axiosInstance';

// Use authAxiosInstance for login, as it's the endpoint that *provides* the token
export const loginUser = async (username, password) => {
    try {
        const response = await authAxiosInstance.post('/login', { username, password });
        return response.data; // Axios wraps the actual response in a 'data' property
    } catch (error) {
        console.error('API login error:', error);
        // Axios errors have a `response` property for server errors
        if (error.response) {
            throw new Error(error.response.data.message || 'Login failed');
        }
        throw error; // Re-throw for network errors or other issues
    }
};

// Use authAxiosInstance for logout. Typically, logout doesn't require sending the JWT
// in the Authorization header to clear server-side context. If your backend's
// logout *invalidates* a token sent in the header, you would use `axiosInstance` here.
export const logoutUser = async () => {
    try {
        const response = await authAxiosInstance.post('/logout');
        return response.data;
    } catch (error) {
        console.error('API logout error:', error);
        if (error.response) {
            throw new Error(error.response.data.message || 'Logout failed on server.');
        }
        throw error;
    }
};

// --- API Calls that now use the interceptor (`axiosInstance`) ---

// Facebook auth-related API calls
export const getFacebookStatus = async () => {
    try {
        // axiosInstance automatically adds the JWT token
        const response = await axiosInstance.get('/auth/facebook/status', {
            withCredentials: true // Equivalent to 'credentials: "include"' for fetch
        });
        return response.data;
    } catch (error) {
        console.error('API facebook status error:', error);
        if (error.response) {
            throw new Error(error.response.data.message || 'Failed to fetch Facebook status');
        }
        throw error;
    }
};

export const getTwitterStatus = async () => {
    try {
        // axiosInstance automatically adds the JWT token
        const response = await axiosInstance.get('/auth/twitter/status', {
            withCredentials: true
        });
        return response.data;
    } catch (error) {
        console.error('API twitter status error:', error);
        if (error.response) {
            throw new Error(error.response.data.message || 'Failed to fetch Twitter status');
        }
        throw error;
    }
};

// These redirect helpers remain largely the same, as they manipulate window.location
// and document.cookie directly for the OAuth flow. They don't use Axios for the redirect.
export const redirectToFacebookOAuth = async () => {
    try {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            throw new Error('You must be logged in!');
        }

        window.location.href = `${authAxiosInstance.defaults.baseURL}/facebook`; // Use baseURL from auth instance
    } catch (error) {
        alert(error.message);
        throw error;
    }
};

export const redirectToTwitterOAuth = async () => {
    try {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            throw new Error('You must be logged in!');
        }
        window.location.href = `${authAxiosInstance.defaults.baseURL}/twitter`; // Use baseURL from auth instance
    } catch (error) {
        alert(error.message);
        throw error;
    }
};

export const postTweet = async (content) => {
    try {
        // axiosInstance automatically adds the JWT token
        const response = await axiosInstance.post('/auth/twitter/tweet', { content }, {
            withCredentials: true
        });
        return response.data;
    } catch (error) {
        console.error('API post tweet error:', error);
        if (error.response) {
            throw new Error(error.response.data.error || 'Failed to post tweet');
        }
        throw error;
    }
};