import React, { useState, useEffect, useRef } from 'react';

/**
 * ChatInterface Component
 * Manages the chat interaction with an AI agent and triggers content generation.
 * @param {object} props - Component props.
 * @param {function} props.onContentGenerated - Callback function to update the generated content in the parent.
 */
function ChatInterface({ onContentGenerated }) {
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

    // Function to handle sending a message and simulating an AI response
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

        // Simulate an AI response after a short delay
        setTimeout(() => {
            const simulatedAiReplyText = `AI Agent: You asked about "${userMessageText}". Here's some simulated social media content: "Excited to share our new product! 🎉 It's designed to boost your productivity. #NewProduct #Innovation"`;
            const aiReply = {
                id: Date.now() + 1, // Ensure unique ID for AI message
                text: simulatedAiReplyText,
                sender: 'ai',
            };
            setMessages((prevMessages) => [...prevMessages, aiReply]);
            onContentGenerated(simulatedAiReplyText); // Update the generated content for the preview panel
            setIsLoading(false); // Hide loading indicator
        }, 1500); // Simulate network delay of 1.5 seconds
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
                            <div className="dot-flashing"></div> {/* Simple loading animation */}
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
                    placeholder={isLoading ? "Simulating response..." : "Type your request..."}
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
                    border-radius: 5px;
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