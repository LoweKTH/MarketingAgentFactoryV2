// src/pages/LandingPage/LandingPage.jsx
import React from 'react';
import Hero from './components/Hero';
import Features from './components/Features';

function LandingPage() {
    return (
        <> {/* Fragment because Navbar/Footer are rendered outside this component */}
            <Hero />
            <Features />
            {/* You might have other sections here like Testimonials, Pricing, etc. */}
        </>
    );
}

export default LandingPage;