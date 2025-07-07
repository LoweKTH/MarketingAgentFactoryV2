// src/api/authApi.js

const API_BASE_URL = 'http://localhost:8080/api/auth'; // Replace with your backend URL

export const loginUser = async (username, password) => {
    try {
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Login failed');
        }

        return data; // Returns { jwt: "...", message: "..." }
    } catch (error) {
        console.error('API login error:', error);
        throw error;
    }
};

export const logoutUser = async () => {
    try {
        // Your Spring Boot /logout endpoint does not require a body or specific headers
        // in your provided controller, as it just clears the SecurityContextHolder.
        // If your logout required invalidating a token, you'd send it in headers.
        const response = await fetch(`${API_BASE_URL}/logout`, {
            method: 'POST', // logout is usually a POST request
            headers: {
                'Content-Type': 'application/json',
                // If your logout endpoint required authorization (e.g., to invalidate a specific token),
                // you would add the Authorization header here with the JWT token.
                // 'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
            },
        });

        const data = await response.json();

        if (!response.ok) {
            // Handle cases where backend logout might fail (though less common for a simple clear context)
            throw new Error(data.message || 'Logout failed on server.');
        }

        return data; // Should return { message: "Logout successful" }
    } catch (error) {
        console.error('API logout error:', error);
        throw error;
    }
};