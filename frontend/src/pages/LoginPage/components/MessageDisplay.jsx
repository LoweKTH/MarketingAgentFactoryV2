// MessageDisplay.jsx
import React from 'react';

function MessageDisplay({ message }) {
    if (!message) return null; // Don't render if there's no message

    const isSuccess = message.includes('successful');
    const bgColorClass = isSuccess ? 'bg-green-100' : 'bg-red-100';
    const textColorClass = isSuccess ? 'text-green-700' : 'text-red-700';

    return (
        <div className={`p-3 mb-4 rounded-lg text-center ${bgColorClass} ${textColorClass}`}>
            {message}
        </div>
    );
}

export default MessageDisplay;