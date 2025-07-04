// LoginPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext'; // Import useAuth hook

// Import the new components
import LoginForm from './components/LoginForm';
import AuthNavigationLinks from './components/AuthNavigationLinks';
import MessageDisplay from './components/MessageDisplay';

function LoginPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const navigate = useNavigate();
    const { login } = useAuth(); // Get the login function from AuthContext

    const handleSubmit = (e) => {
        e.preventDefault();
        if (email === 'user@example.com' && password === 'password123') { // Simple mock authentication
            setMessage('Login successful! Redirecting to content...');
            login(); // Call the login function from context
            setTimeout(() => {
                navigate('/content');
            }, 1500);
        } else {
            setMessage('Invalid email or password.');
        }
    };

    return (
        <div className="flex items-center justify-center min-h-[calc(100vh-64px)] bg-gray-100 p-4">
            <div className="bg-white p-8 rounded-xl shadow-lg w-full max-w-md border border-gray-200">
                <h2 className="text-3xl font-bold text-center text-indigo-700 mb-8">Login / Sign Up</h2>

                <MessageDisplay message={message} />

                <LoginForm
                    email={email}
                    setEmail={setEmail}
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
