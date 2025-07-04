// src/components/Navbar.jsx
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext'; // Import useAuth hook

function Navbar() {
    const navigate = useNavigate();
    const { isAuthenticated, logout } = useAuth(); // Get isAuthenticated and logout from context

    const handleLogout = () => {
        logout(); // Call the logout function from context
        navigate('/'); // Redirect to landing page after logout
    };

    return (
        <nav className="bg-white shadow-sm p-4 sticky top-0 z-50">
            <div className="container mx-auto flex justify-between items-center">
                {/* Logo/Brand Name - now uses navigate to go home */}
                <button onClick={() => navigate('/')} className="text-2xl font-bold text-indigo-600 focus:outline-none cursor-pointer">
                    MyBrand
                </button>

                {/* Navigation Links */}
                <div className="space-x-6 hidden md:block">
                    <button onClick={() => navigate('/')} className="text-gray-600 hover:text-indigo-600 transition duration-300 focus:outline-none">Features</button>
                    <button onClick={() => navigate('/')} className="text-gray-600 hover:text-indigo-600 transition duration-300 focus:outline-none">Pricing</button>
                    <button onClick={() => navigate('/')} className="text-gray-600 hover:text-indigo-600 transition duration-300 focus:outline-none">Contact</button>
                </div>

                {/* Conditional rendering for Sign Up / Logout button */}
                {isAuthenticated ? (
                    <button
                        onClick={handleLogout}
                        className="bg-red-600 text-white py-2 px-4 rounded-lg hover:bg-red-700 transition duration-300 hidden md:block focus:outline-none cursor-pointer"
                    >
                        Logout
                    </button>
                ) : (
                    <button
                        onClick={() => navigate('/login')}
                        className="bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition duration-300 hidden md:block focus:outline-none cursor-pointer"
                    >
                        Log In
                    </button>
                )}

                {/* Mobile Menu Icon (for later implementation) */}
                <button className="md:hidden text-gray-600 focus:outline-none">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16"></path>
                    </svg>
                </button>
            </div>
        </nav>
    );
}

export default Navbar;
