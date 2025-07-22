// src/contexts/AuthContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';
import { logoutUser } from '../api/authApi'; // Import the logout API function

// Create the AuthContext
const AuthContext = createContext(null);

// Custom hook to use the AuthContext
export const useAuth = () => {
    return useContext(AuthContext);
};

// AuthProvider component to wrap your application
export const AuthProvider = ({ children }) => {
    // Initialize isAuthenticated from localStorage to persist login state across refreshes
    const [isAuthenticated, setIsAuthenticated] = useState(() => {
        const storedAuth = localStorage.getItem('isAuthenticated');
        return storedAuth === 'true'; // Convert string 'true' to boolean true
    });

    // Function to handle user login
    const login = () => {
        setIsAuthenticated(true);
        localStorage.setItem('isAuthenticated', 'true'); // Store login state
        console.log("User logged in.");
    };

    // Function to handle user logout
    const logout = async () => { // Make logout async
        try {
            await logoutUser(); // Call the backend logout endpoint
            setIsAuthenticated(false);
            localStorage.removeItem('isAuthenticated'); // Remove login state
            localStorage.removeItem('jwtToken'); // **Crucially, remove the JWT token**
            console.log("User logged out successfully from frontend and backend.");
        } catch (error) {
            console.error("Error during logout:", error);
            // Optionally, even if backend logout fails, you might want to clear frontend state
            // to avoid inconsistencies, especially if the token is already invalid.
            setIsAuthenticated(false);
            localStorage.removeItem('isAuthenticated');
            localStorage.removeItem('jwtToken');
            // You might also display an error message to the user here.
        }
    };

    // The value provided to consumers of this context
    const value = {
        isAuthenticated,
        login,
        logout,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};