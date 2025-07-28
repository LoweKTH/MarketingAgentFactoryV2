// src/components/Navbar.jsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

function Navbar() {
    const navigate = useNavigate();
    const { isAuthenticated, logout } = useAuth();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <nav className="bg-white shadow-sm p-4 sticky top-0 z-50">
            <div className="container mx-auto flex justify-between items-center">
                {/* Logo/Brand Name - Left */}
                <button
                    onClick={() => navigate('/')}
                    className="text-2xl font-bold text-indigo-600 focus:outline-none cursor-pointer"
                >
                    MyBrand
                </button>

                {/* Navigation Links - Center */}
                <div className="flex space-x-6 hidden md:flex">
                    <button
                        onClick={() => navigate('/')}
                        className="text-gray-600 hover:text-indigo-600 transition duration-300 focus:outline-none"
                    >
                        Features
                    </button>
                    <button
                        onClick={() => navigate('/')}
                        className="text-gray-600 hover:text-indigo-600 transition duration-300 focus:outline-none"
                    >
                        Pricing
                    </button>
                    <button
                        onClick={() => navigate('/')}
                        className="text-gray-600 hover:text-indigo-600 transition duration-300 focus:outline-none"
                    >
                        Contact
                    </button>
                </div>

                {/* Profile and Auth Buttons - Right */}
                <div className="flex items-center space-x-4 hidden md:flex">
                    <button
                        onClick={() => navigate('/profile')}
                        className="bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition duration-300 focus:outline-none"
                    >
                        Profile
                    </button>

                    {isAuthenticated ? (
                        <button
                            onClick={handleLogout}
                            className="bg-red-600 text-white py-2 px-4 rounded-lg hover:bg-red-700 transition duration-300 focus:outline-none"
                        >
                            Logout
                        </button>
                    ) : (
                        <button
                            onClick={() => navigate('/login')}
                            className="bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition duration-300 focus:outline-none"
                        >
                            Log In
                        </button>
                    )}
                </div>

                {/* Mobile Menu Icon */}
                <button className="md:hidden text-gray-600 focus:outline-none">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16"></path>
                    </svg>
                </button>
            </div>
        </nav>
    );
}

export default Navbar;