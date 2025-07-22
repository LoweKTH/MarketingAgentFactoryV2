import React from 'react';
import PostButton from './PostButton.jsx'; // Import the PostButton component

/**
 * PreviewPanel Component
 * Displays the AI-generated social media content in a styled card.
 * @param {object} props - Component props.
 * @param {string} props.content - The social media content string to display.
 * @param {function} [props.onPostClick] - Optional callback for when the post button is clicked.
 * @param {boolean} [props.isPosting] - Optional prop to indicate if a post is in progress.
 */
function PreviewPanel({ content, onPostClick, isPosting }) { // Destructure onPostClick and isPosting
    return (
        <div className="flex flex-col h-full p-4">
            <h2 className="text-2xl font-bold mb-6 text-gray-800 text-center">Generated Content Preview</h2>
            <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md mx-auto flex-grow overflow-hidden flex flex-col">
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
                        {/* Adjusted max-h for dynamic height, added overflow-y-auto */}
                        <div className="flex-grow overflow-y-auto pr-2">
                            <p className="text-gray-700 whitespace-pre-wrap">{content}</p>
                        </div>
                        {/* Removed Like, Comment, Share */}
                        {/* <div className="mt-4 text-gray-500 text-sm flex justify-between">
                            <span>❤️ Like</span>
                            <span>💬 Comment</span>
                            <span>🔄 Share</span>
                        </div> */}
                        {/* Add the PostButton here */}
                        <div className="mt-6"> {/* Added margin-top for spacing */}
                            <PostButton
                                onClick={onPostClick} // Pass the onClick handler
                                isLoading={isPosting}  // Pass the loading state
                            />
                        </div>
                    </>
                ) : (
                    <div className="text-center text-gray-500 py-10 flex-grow flex items-center justify-center">
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