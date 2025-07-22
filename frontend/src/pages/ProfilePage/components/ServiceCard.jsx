import React from 'react';

const ServiceCard = ({ title, description, icon, children }) => {
    return (
        <div className="bg-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-shadow duration-300">
            <h2 className="text-2xl font-bold text-gray-800 mb-4 flex items-center">
                {icon}
                {title}
            </h2>
            <p className="text-gray-600 mb-4">{description}</p>
            <div className="space-y-3">
                {children}
            </div>
        </div>
    );
};

export default ServiceCard;