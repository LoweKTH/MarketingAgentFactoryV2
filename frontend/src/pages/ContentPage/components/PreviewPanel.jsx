import React from 'react';

/**
 * PreviewPanel Component
 * Displays the AI-generated social media content in a styled card.
 * @param {object} props - Component props.
 * @param {string} props.content - The social media content string to display.
 */
function PreviewPanel({ content }) {
    return (
        <div className="flex flex-col items-center justify-center h-full p-4">
            <h2 className="text-2xl font-bold mb-6 text-gray-800">Generated Content Preview</h2>
            <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
                {content ? (
                    <>
                        {/* Simulate a social media post structure */}
                        <div className="flex items-center mb-4">
                            <div className="w-10 h-10 bg-blue-400 rounded-full flex items-center justify-center text-white font-bold text-lg">
                                AI
                            </div>
                            <div className="ml-3">
                                <p className="font-semibold text-gray-900">AI Agent</p>
                                <p className="text-sm text-gray-500">Just now</p>
                            </div>
                        </div>
                        <p className="text-gray-700 whitespace-pre-wrap">{content}</p> {/* whitespace-pre-wrap to preserve newlines */}
                        <div className="mt-4 text-gray-500 text-sm flex justify-between">
                            <span>❤️ Like</span>
                            <span>💬 Comment</span>
                            <span>🔄 Share</span>
                        </div>
                    </>
                ) : (
                    <div className="text-center text-gray-500 py-10">
                        Your generated social media content will appear here.
                        <br />
                        Start by chatting with the AI!
                    </div>
                )}
            </div>
        </div>
    );
}

export default PreviewPanel;
