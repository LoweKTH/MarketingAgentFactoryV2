import React from 'react';

/**
 * PostButton Component
 * A reusable button for posting content, with loading state.
 * @param {object} props - Component props.
 * @param {function} props.onClick - Function to call when the button is clicked.
 * @param {boolean} props.isLoading - Whether the button should show a loading state.
 */
function PostButton({ onClick, isLoading }) {
    return (
        <button
            onClick={onClick}
            disabled={isLoading} // Disable button when loading
            className={`
                mt-4 w-full py-3 px-6 rounded-lg text-white font-semibold
                transition-all duration-300 ease-in-out
                ${isLoading
                ? 'bg-blue-400 cursor-not-allowed' // Slightly adjusted disabled color
                : 'bg-blue-600 hover:bg-blue-700 active:bg-blue-800 shadow-md hover:shadow-lg' // Adjusted active color
            }
                flex items-center justify-center
            `}
        >
            {isLoading ? (
                // Simple loading spinner (Tailwind CSS animation)
                <svg className="animate-spin h-5 w-5 text-white mr-3" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
            ) : (
                <>
                    {/* Lucide React Share2 icon (requires lucide-react to be installed) */}
                    {/* If lucide-react is not available, you can use a simple SVG or emoji */}
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-2">
                        <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.59 13.51l6.83 4.98"/><path d="M15.41 6.49l-6.83 4.98"/>
                    </svg>
                    Post to Social Media
                </>
            )}
        </button>
    );
}

export default PostButton;
