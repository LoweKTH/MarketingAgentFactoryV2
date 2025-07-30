import React from 'react';

function MessageDisplay({ message }) {
    if (!message) return null; // Don't render if there's no message

    const isSuccess = message.includes('successful');
    // Adjusted colors for dark theme readability
    const bgColorClass = isSuccess ? 'bg-green-700' : 'bg-red-700';
    const textColorClass = isSuccess ? 'text-green-100' : 'text-red-100';

    return (
        <div className={`p-3 mb-4 rounded-lg text-center ${bgColorClass} ${textColorClass}`}>
            {message}
        </div>
    );
}

export default MessageDisplay;
