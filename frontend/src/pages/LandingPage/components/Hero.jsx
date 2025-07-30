// src/components/Hero.jsx
import React from 'react';
import { Link } from 'react-router-dom';

function Hero() {
    return (
        // Removed specific background color to let parent gradient show through
        <section className="text-white py-20 md:py-32 flex items-center justify-center min-h-screen">
            <div className="container mx-auto text-center px-4">
                <h1 className="text-4xl md:text-6xl font-extrabold leading-tight mb-4 animate-fade-in-up">
                    Unlock Your Potential with Our Amazing Product
                </h1>
                <p className="text-lg md:text-xl mb-8 opacity-90 animate-fade-in-up delay-200 text-gray-200">
                    Streamline your workflow, boost productivity, and achieve your goals faster than ever before.
                </p>
                <Link to="/chat-select" className="inline-block">
                    <button className="bg-indigo-600 text-white font-bold py-3 px-8 rounded-full shadow-lg hover:bg-indigo-700 hover:scale-105 transform transition duration-300 animate-fade-in-up delay-400 cursor-pointer">
                        Get Started For Free
                    </button>
                </Link>
            </div>
        </section>
    );
}

export default Hero;
