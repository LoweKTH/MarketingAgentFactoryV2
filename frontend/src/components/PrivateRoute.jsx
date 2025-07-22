// src/components/PrivateRoute.jsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext'; // Import useAuth hook

function PrivateRoute({ children }) {
    const { isAuthenticated } = useAuth(); // Get authentication status from context

    // If the user is authenticated, render the children (the protected component)
    // Otherwise, redirect them to the login page
    return isAuthenticated ? children : <Navigate to="/login" replace />;
}

export default PrivateRoute;
