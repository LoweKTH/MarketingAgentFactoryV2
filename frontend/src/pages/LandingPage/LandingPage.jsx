// src/pages/LandingPage/LandingPage.jsx
import React from 'react';
import Hero from './components/Hero';
import Features from './components/Features';

function LandingPage() {
    return (
        // Added a gradient background to the main container
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-950 dark:to-blue-950 font-sans">
            <Hero />
            <Features />
            {/* You might have other sections here like Testimonials, Pricing, etc. */}
        </div>
    );
}

export default LandingPage;
