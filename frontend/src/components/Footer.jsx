
import React from 'react';

function Footer() {
    return (
        <footer className="bg-gray-800 text-white py-10">
            <div className="container mx-auto px-4 text-center">
                <div className="flex flex-col md:flex-row justify-between items-center mb-6">
                    {/* Logo/Brand Name */}
                    <a href="#" className="text-2xl font-bold text-white mb-4 md:mb-0">MyBrand</a>

                    {/* Footer Navigation */}
                    <div className="flex space-x-6 mb-4 md:mb-0">
                        <a href="#" className="text-gray-400 hover:text-white transition duration-300">Privacy Policy</a>
                        <a href="#" className="text-gray-400 hover:text-white transition duration-300">Terms of Service</a>
                        <a href="#" className="text-gray-400 hover:text-white transition duration-300">FAQ</a>
                    </div>

                    {/* Social Media Icons (placeholders) */}
                    <div className="flex space-x-4 text-gray-400">
                        <a href="#" className="hover:text-white transition duration-300">
                            <i className="fab fa-facebook-f"></i> {/* Requires Font Awesome or similar */}
                        </a>
                        <a href="#" className="hover:text-white transition duration-300">
                            <i className="fab fa-twitter"></i>
                        </a>
                        <a href="#" className="hover:text-white transition duration-300">
                            <i className="fab fa-linkedin-in"></i>
                        </a>
                    </div>
                </div>
                <p className="text-gray-400 text-sm mt-4">
                    &copy; {new Date().getFullYear()} MyBrand. All rights reserved.
                </p>
            </div>
        </footer>
    );
}

export default Footer;