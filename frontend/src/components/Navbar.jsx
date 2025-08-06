import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext'; // Assuming this path is correct

function Navbar() {
    const navigate = useNavigate();
    const { isAuthenticated, logout } = useAuth();

    // Function to handle logout and navigation
    const handleLogout = () => {
        logout();
        navigate('/');
    };

    // Function to handle navigation
    const handleNavigation = (path) => {
        navigate(path);
    };

    return (
        <nav className="bg-gray-800 text-white shadow-sm p-4 sticky top-0 z-50 font-sans">
            <div className="container mx-auto flex justify-between items-center">
                {/* Logo/Brand Name - Left */}
                <button
                    onClick={() => handleNavigation('/')}
                    className="text-2xl font-bold text-white focus:outline-none cursor-pointer rounded-md p-2 hover:bg-gray-700 transition duration-300"
                >
                    MyBrand
                </button>

                {/* Action Buttons - Right */}
                <div className="flex items-center space-x-4">
                    {/* Chat with Agent Button */}
                    <button
                        onClick={() => handleNavigation('/chat-select')}
                        className="bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-green-700 transition duration-300 focus:outline-none"
                    >
                        Chat with Agent
                    </button>

                    {/* Scheduled Tasks Button */}
                    <button
                        onClick={() => handleNavigation('/task')}
                        className="bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-green-700 transition duration-300 focus:outline-none"
                    >
                        Scheduled Tasks
                    </button>

                    {/* Login/Logout Button */}
                    {isAuthenticated ? (
                        <button
                            onClick={handleLogout}
                            className="bg-red-600 text-white py-2 px-4 rounded-lg hover:bg-red-700 transition duration-300 focus:outline-none"
                        >
                            Logout
                        </button>
                    ) : (
                        <button
                            onClick={() => handleNavigation('/login')}
                            className="bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition duration-300 focus:outline-none"
                        >
                            Log In
                        </button>
                    )}
                </div>
            </div>
        </nav>
    );
}

export default Navbar;