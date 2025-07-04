// src/components/Features.jsx
import React from 'react';

function Features() {
    const features = [
        {
            icon: '📊',
            title: 'Advanced Analytics',
            description: 'Gain deep insights into your data with our powerful analytics tools.',
        },
        {
            icon: '⚡',
            title: 'Blazing Fast Performance',
            description: 'Experience unparalleled speed and responsiveness in all your tasks.',
        },
        {
            icon: '💡',
            title: 'Intuitive Interface',
            description: 'Designed for ease of use, so you can focus on what matters most.',
        },
        {
            icon: '🔒',
            title: 'Top-tier Security',
            description: 'Your data is safe with us, protected by industry-leading security measures.',
        },
    ];

    return (
        <section id="features" className="py-16 md:py-24 bg-white">
            <div className="container mx-auto px-4 text-center">
                <h2 className="text-3xl md:text-4xl font-bold mb-12 text-gray-900">
                    Why Choose Us?
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                    {features.map((feature, index) => (
                        <div
                            key={index}
                            className="bg-gray-50 p-8 rounded-lg shadow-md hover:shadow-lg transition duration-300 transform hover:scale-105"
                        >
                            <div className="text-5xl mb-4">{feature.icon}</div>
                            <h3 className="text-xl font-semibold mb-2 text-gray-800">{feature.title}</h3>
                            <p className="text-gray-600">{feature.description}</p>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}

export default Features;