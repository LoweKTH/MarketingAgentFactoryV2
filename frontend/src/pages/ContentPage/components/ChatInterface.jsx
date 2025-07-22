import React, { useState, useEffect, useRef } from 'react';
import { generateContentPlan, createContentRequest, extractContentFromResponse, isAuthenticated } from '../../../api/agentApi';

/**
 * ChatInterface Component
 * Manages the chat interaction with an AI agent and triggers content generation.
 * @param {object} props - Component props.
 * @param {function} props.onContentGenerated - Callback function to update the generated content in the parent.
 */
function ChatInterface({ onContentGenerated,  selectedPlatform, selectedTone }) {
    // State to hold the chat messages
    const [messages, setMessages] = useState([]);
    // State to hold the current input value
    const [input, setInput] = useState('');
    // State to manage loading indicator for AI response
    const [isLoading, setIsLoading] = useState(false);
    // Ref for auto-scrolling chat to the bottom
    const messagesEndRef = useRef(null);
    // Ref for the scrollable chat container
    const chatContainerRef = useRef(null);



    useEffect(() => {
        // Use the chat container's scrollTop instead of scrollIntoView to prevent page scrolling
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
    }, [messages]);

    // Function to call the backend API using the separate API service
    const callAgentAPI = async (userMessage) => {
        if (!isAuthenticated()) {
            throw new Error('Please login to generate content');
        }


        const targetPlatform = selectedPlatform;
        const extraInstructions = `Tone: ${selectedTone}.`;

        // Create content request object using the updated signature
        const contentRequest = createContentRequest(
            userMessage, // This is your topic
            targetPlatform,
            extraInstructions
        );

        // Call the API service
        return await generateContentPlan(contentRequest);
    };
    // Function to handle sending a message and getting AI response from API
    const handleSendMessage = async () => {
        if (input.trim() === '') {
            return; // Don't send empty messages
        }

        const userMessageText = input.trim();
        const newUserMessage = { id: Date.now(), text: userMessageText, sender: 'user' };

        // Add user message to chat immediately
        setMessages((prevMessages) => [...prevMessages, newUserMessage]);
        setInput(''); // Clear input after sending
        setIsLoading(true); // Show loading indicator

        try {
            // Call the actual API using the API service
            const response = await callAgentAPI(userMessageText);

            // Log the full response for debugging
            console.log('Full API Response in ChatInterface:', response);

            // Extract content from response using helper function
            const contentForDisplay = extractContentFromResponse(response);
            console.log('Extracted content for display:', contentForDisplay);

            // Create AI response from API response
            const aiReply = {
                id: Date.now() + 1, // Ensure unique ID for AI message
                text: contentForDisplay,
                sender: 'ai',
            };

            // Add AI response to chat
            setMessages((prevMessages) => [...prevMessages, aiReply]);

            // Send the generated content to the preview panel
            onContentGenerated(contentForDisplay);

        } catch (error) {
            // Handle API errors with more specific error messages
            let errorMessage = 'Sorry, there was an error generating content. Please try again.';

            if (error.message.includes('login')) {
                errorMessage = 'Please login to generate content.';
            } else if (error.message.includes('Authentication failed')) {
                errorMessage = 'Your session has expired. Please login again.';
            } else if (error.message.includes('Server error')) {
                errorMessage = 'Server is temporarily unavailable. Please try again later.';
            }

            const errorResponse = {
                id: Date.now() + 1,
                text: errorMessage,
                sender: 'ai',
            };
            setMessages((prevMessages) => [...prevMessages, errorResponse]);
        } finally {
            setIsLoading(false); // Hide loading indicator
        }
    };

    // Function to handle input change
    const handleInputChange = (e) => {
        setInput(e.target.value);
    };

    // Function to handle pressing Enter key in the input
    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !isLoading) {
            handleSendMessage();
        }
    };

    return (
        <div className="flex flex-col h-full bg-white rounded-lg shadow-md overflow-hidden">


            {/* Chat Display Area */}
            <div
                ref={chatContainerRef}
                className="flex-1 min-h-0 overflow-y-auto p-4 space-y-4 break-words"
            >
                {messages.length === 0 ? (
                    <div className="text-center text-gray-500 py-10">
                        Tell me what kind of social media content you need!
                    </div>
                ) : (
                    messages.map((message) => (
                        <div
                            key={message.id}
                            className={`flex ${
                                message.sender === 'user' ? 'justify-end' : 'justify-start'
                            }`}
                        >
                            <div
                                className={`p-3 rounded-lg max-w-[70%] ${
                                    message.sender === 'user'
                                        ? 'bg-blue-500 text-white'
                                        : 'bg-gray-200 text-gray-800'
                                } shadow-sm`}
                            >
                                {message.text}
                            </div>
                        </div>
                    ))
                )}
                {isLoading && (
                    <div className="flex justify-start">
                        <div className="p-3 rounded-lg bg-gray-200 text-gray-800 shadow-sm">
                            <div className="dot-flashing"></div> {/* Loading animation */}
                        </div>
                    </div>
                )}
                <div ref={messagesEndRef} /> {/* Element to scroll into view */}
            </div>

            {/* Input Area */}
            <div className="border-t border-gray-200 p-4 flex items-center">
                <input
                    type="text"
                    value={input}
                    onChange={handleInputChange}
                    onKeyPress={handleKeyPress}
                    className="flex-1 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder={isLoading ? "Generating response..." : "Type your request..."}
                    disabled={isLoading} // Disable input while loading
                />
                <button
                    onClick={handleSendMessage}
                    className="ml-4 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                    disabled={isLoading} // Disable button while loading
                >
                    {isLoading ? 'Sending...' : 'Send'}
                </button>
            </div>

            {/* Basic CSS for loading animation */}
            <style jsx>{`
                .dot-flashing {
                    position: relative;
                    width: 10px;
                    height: 10px;
                    border-radius: 5px;
                    background-color: #9880ff;
                    color: #9880ff;
                    animation: dotFlashing 1s infinite linear alternate;
                    animation-delay: 0.5s;
                }
                .dot-flashing::before, .dot-flashing::after {
                    content: '';
                    display: inline-block;
                    position: absolute;
                    top: 0;
                }
                .dot-flashing::before {
                    left: -15px;
                    width: 10px;
                    height: 10px;
                    border-radius: 5px;
                    background-color: #9880ff;
                    color: #9880ff;
                    animation: dotFlashing 1s infinite linear alternate;
                    animation-delay: 0s;
                }
                .dot-flashing::after {
                    left: 15px;
                    width: 10px;
                    height: 10px;
                    border-color: 5px;
                    background-color: #9880ff;
                    color: #9880ff;
                    animation: dotFlashing 1s infinite linear alternate;
                    animation-delay: 1s;
                }
                @keyframes dotFlashing {
                    0% {
                        background-color: #9880ff;
                    }
                    50%,
                    100% {
                        background-color: #ebe6ff;
                    }
                }
            `}</style>
        </div>
    );
}

export default ChatInterface;