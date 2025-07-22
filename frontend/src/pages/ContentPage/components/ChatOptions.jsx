// src/components/ChatOptions.jsx
import React from 'react';

function ChatOptions({ selectedPlatform, onPlatformChange, selectedTone, onToneChange, isDisabled }) {
    // Define platform and tone options here or pass them as props
    const platformOptions = [
        { value: 'general', label: 'General' },
        { value: 'facebook', label: 'Facebook' },
        { value: 'instagram', label: 'Instagram' },
        { value: 'linkedin', label: 'LinkedIn' },
        { value: 'twitter', label: 'Twitter/X' },
        { value: 'blog', label: 'Blog Post' },
        { value: 'email', label: 'Email' },
    ];

    const toneOptions = [
        { value: 'neutral', label: 'Neutral' },
        { value: 'formal', label: 'Formal' },
        { value: 'informal', label: 'Informal' },
        { value: 'friendly', label: 'Friendly' },
        { value: 'professional', label: 'Professional' },
        { value: 'witty', label: 'Witty' },
        { value: 'empathetic', label: 'Empathetic' },
    ];

    return (
        <div className="p-4 bg-gray-100 border-b border-gray-200 flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
                <label htmlFor="platform-select" className="block text-sm font-medium text-gray-700 mb-1">
                    Select Platform:
                </label>
                <select
                    id="platform-select"
                    value={selectedPlatform}
                    onChange={onPlatformChange}
                    className="block w-full p-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    disabled={isDisabled}
                >
                    {platformOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                            {option.label}
                        </option>
                    ))}
                </select>
            </div>
            <div className="flex-1">
                <label htmlFor="tone-select" className="block text-sm font-medium text-gray-700 mb-1">
                    Select Tone:
                </label>
                <select
                    id="tone-select"
                    value={selectedTone}
                    onChange={onToneChange}
                    className="block w-full p-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    disabled={isDisabled}
                >
                    {toneOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                            {option.label}
                        </option>
                    ))}
                </select>
            </div>
        </div>
    );
}

export default ChatOptions;