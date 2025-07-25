// LoginPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

// Import the API function
import { loginUser } from '../../api/authApi'; // Adjust path if needed

import LoginForm from './components/LoginForm';
import AuthNavigationLinks from './components/AuthNavigationLinks';
import MessageDisplay from './components/MessageDisplay';

function LoginPage() {
    const [username, setUsername] = useState(''); // Changed from email to username
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');

        try {
            const data = await loginUser(username, password); // Pass username instead of email

            setMessage(data.message);
            login(); // Call the login function from context
            // You should also handle storing data.jwt here for future authenticated requests
            localStorage.setItem('jwtToken', data.jwt); // Example: store JWT token

            // document.cookie = `jwt=${data.jwt}; path=/; max-age=86400; SameSite=Lax; HttpOnly; Secure`; // Recommended for production (if not set by backend)
            // For development, if you're not using HTTPS or HttpOnly, you might use:
            document.cookie = `jwt=${data.jwt}; path=/; max-age=86400; SameSite=Lax`; // Adjust SameSite/Secure/HttpOnly based on your backend setup and environment
            console.log(data.jwt);
            setTimeout(() => {
                navigate('/content');
            }, 1500);
        } catch (error) {
            // The error object thrown by loginUser will contain the backend message
            setMessage(error.message || 'An unexpected error occurred during login.');
        }
    };

    return (
        <div className="flex items-center justify-center min-h-[calc(100vh-64px)] bg-gray-100 p-4">
            <div className="bg-white p-8 rounded-xl shadow-lg w-full max-w-md border border-gray-200">
                <h2 className="text-3xl font-bold text-center text-indigo-700 mb-8">Login / Sign Up</h2>

                <MessageDisplay message={message} />

                <LoginForm
                    username={username} // Pass username prop
                    setUsername={setUsername} // Pass setUsername prop
                    password={password}
                    setPassword={setPassword}
                    handleSubmit={handleSubmit}
                />

                <AuthNavigationLinks setMessage={setMessage} />
            </div>
        </div>
    );
}

export default LoginPage;