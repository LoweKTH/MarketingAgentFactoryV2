import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext'; // Assuming this path is correct

function Navbar() {
    const navigate = useNavigate();
    const { isAuthenticated, logout } = useAuth();
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef(null);

    // Function to handle logout
    const handleLogout = () => {
        logout();
        navigate('/');
    };

    // Function to close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsDropdownOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    // Function to navigate and close dropdown
    const handleNavigation = (path) => {
        navigate(path);
        setIsDropdownOpen(false); // Close dropdown after navigation
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



                {/* Profile and Auth Buttons - Right */}
                <div className="flex items-center space-x-4">
                    {/* Dropdown for Task, Profile, Conversations */}
                    <div className="relative" ref={dropdownRef}>
                        <button
                            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                            className="bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition duration-300 focus:outline-none flex items-center space-x-2"
                        >
                            <span>Menu</span>
                            <svg
                                className={`w-4 h-4 transform transition-transform duration-200 ${isDropdownOpen ? 'rotate-180' : 'rotate-0'}`}
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                                xmlns="http://www.w3.org/2000/svg"
                            >
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path>
                            </svg>
                        </button>

                        {isDropdownOpen && (
                            <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-10">
                                <button
                                    onClick={() => handleNavigation('/task')}
                                    className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100 rounded-md transition duration-200"
                                >
                                    Scheduled Tasks
                                </button>
                                <button
                                    onClick={() => handleNavigation('/profile')}
                                    className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100 rounded-md transition duration-200"
                                >
                                    Profile
                                </button>
                                <button
                                    onClick={() => handleNavigation('/chat-select')}
                                    className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100 rounded-md transition duration-200"
                                >
                                    Chat with Agent
                                </button>
                            </div>
                        )}
                    </div>

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

                {/* Mobile Menu Icon (You might want to implement a mobile menu for these too) */}
                <button className="md:hidden text-white focus:outline-none">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16"></path>
                    </svg>
                </button>
            </div>
        </nav>
    );
}

export default Navbar;
