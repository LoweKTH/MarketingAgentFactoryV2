// AuthNavigationLinks.jsx
import React from 'react';
import { useNavigate } from 'react-router-dom';

function AuthNavigationLinks({ setMessage }) {
    const navigate = useNavigate();

    return (
        <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
                Don't have an account?{' '}
                <button
                    onClick={() => setMessage('Sign up functionality would be here!')} // Placeholder
                    className="font-medium text-indigo-600 hover:text-indigo-500 focus:outline-none"
                >
                    Sign Up
                </button>
            </p>
            <p className="mt-2 text-sm text-gray-600">
                <button
                    onClick={() => setMessage('Forgot password functionality would be here!')} // Placeholder
                    className="font-medium text-indigo-600 hover:text-indigo-500 focus:outline-none"
                >
                    Forgot Password?
                </button>
            </p>
            <button
                onClick={() => navigate('/')} // Navigate back to the landing page
                className="mt-4 text-sm font-medium text-gray-500 hover:text-gray-700 focus:outline-none"
            >
                ← Back to Landing Page
            </button>
        </div>
    );
}

export default AuthNavigationLinks;