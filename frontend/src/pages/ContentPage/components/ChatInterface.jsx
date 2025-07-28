import React, { useState, useEffect, useRef } from 'react';
import { sendChatMessage, fetchConversationMessages } from '../../../api/chatApi'; // Import fetchConversationMessages and sendChatMessage
import { marked } from 'marked'; // Import marked for Markdown rendering
import DOMPurify from 'dompurify'; // Import DOMPurify for sanitizing HTML

// Helper function to sanitize and render Markdown
const renderMarkdown = (text) => {
    // Handle potential null/undefined text
    const formattedText = text ? String(text).replace(/\n/g, '<br>') : '';
    const rawMarkup = marked(formattedText);
    return DOMPurify.sanitize(rawMarkup);
};

/**
 * ChatInterface Component
 * Manages the chat interaction with an AI agent and triggers content generation.
 * @param {object} props - Component props.
 * @param {function} props.onContentGenerated - Callback function to update the generated content in the parent.
 * @param {string} props.selectedPlatform - The currently selected platform.
 * @param {string} props.selectedTone - The currently selected tone.
 * @param {boolean} props.isLoading - Whether content generation is in progress (from parent).
 * @param {function} props.setIsLoading - Setter for parent's isLoading state.
 * @param {number|null} props.conversationId - The ID of the current conversation.
 * @param {Array<object>} props.initialMessages - Messages loaded from an existing conversation.
 * @param {function} props.setCurrentConversationId - Setter for parent's currentConversationId state.
 */
function ChatInterface({
                           onContentGenerated,
                           selectedPlatform, // Still received, but not sent to backend for handleChatMessage
                           selectedTone,     // Still received, but not sent to backend for handleChatMessage
                           isLoading,
                           setIsLoading,
                           conversationId,
                           initialMessages,
                           setCurrentConversationId,
                       }) {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const chatContainerRef = useRef(null);

    // --- EFFECT: Initialize messages when conversationId or initialMessages change ---
    useEffect(() => {
        const loadConversationHistory = async () => {
            if (conversationId) { // If a conversation ID exists, fetch its messages
                setIsLoading(true); // Use parent's isLoading for initial fetch
                try {
                    const fetchedMessages = await fetchConversationMessages(conversationId);
                    const formattedMessages = fetchedMessages.map(msg => ({
                        id: msg.id,
                        text: msg.message,
                        sender: msg.role.toLowerCase(),
                        creationTimestamp: msg.creationTimestamp,
                    }));
                    setMessages(formattedMessages);
                } catch (error) {
                    console.error("Failed to load conversation messages:", error);
                    setMessages([{ id: Date.now(), text: "Error loading conversation history. Please try again.", sender: 'ai' }]);
                } finally {
                    setIsLoading(false); // Use parent's isLoading for initial fetch
                }
            } else {
                setMessages([]); // If conversationId is null, it's a new conversation, so clear messages
            }
        };

        // This ensures initialMessages (if provided by ContentPage) are used immediately
        // while the full history might still be loading or if it's a new conversation.
        if (initialMessages && initialMessages.length > 0) {
            const formatted = initialMessages.map(msg => ({
                id: msg.id,
                text: msg.message,
                sender: msg.role.toLowerCase(),
                creationTimestamp: msg.creationTimestamp,
            }));
            setMessages(formatted);
        } else {
            loadConversationHistory(); // Trigger fetch if no initial messages or new conversation
        }
    }, [conversationId, initialMessages]); // Dependency array: re-run if ID or initial messages change

    // --- EFFECT: Auto-scroll chat to the bottom ---
    useEffect(() => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
    }, [messages, isLoading]); // Scroll when messages or loading state changes

    // Function to handle sending a message and getting AI response from API
    const handleSendMessage = async () => {
        if (input.trim() === '') {
            return; // Don't send empty messages
        }

        const userMessageText = input.trim();
        const tempUserMessage = { id: Date.now(), text: userMessageText, sender: 'user', creationTimestamp: new Date().toISOString() };

        setMessages((prevMessages) => [...prevMessages, tempUserMessage]);
        setInput('');
        setIsLoading(true); // Show loading indicator (controlled by parent ContentPage)

        try {
            // Call the API service function, sending only message and conversationId
            const responseData = await sendChatMessage(
                conversationId, // Will be null for new conv, or existing ID
                userMessageText // Only send the message text
            );

            console.log('Full API Response in ChatInterface (sendChatMessage):', responseData);

            // If it was a new conversation, and the backend returned a new ID, update parent state
            if (conversationId === null && responseData.conversationId) {
                setCurrentConversationId(responseData.conversationId);
            }

            // The backend's ChatMessageResponse contains 'agentResponse'
            const aiReply = {
                id: Date.now() + 1, // Temporary ID for AI message, assuming backend doesn't return full DTO for AI reply
                text: responseData.agentResponse, // Use 'agentResponse' from backend DTO
                sender: 'ai',
                creationTimestamp: new Date().toISOString(),
            };

            setMessages((prevMessages) => [...prevMessages, aiReply]);
            onContentGenerated(responseData.agentResponse); // Pass the agent's response for preview

        } catch (error) {
            let errorMessage = 'Sorry, there was an error generating content. Please try again.';

            if (error.message && (error.message.includes('login') || error.message.includes('Authentication failed'))) {
                errorMessage = 'Your session has expired or you are not logged in. Please log in again.';
            } else if (error.message && (error.message.includes('Server error') || error.message.includes('Network Error'))) {
                errorMessage = 'The server is temporarily unavailable or encountered an issue. Please try again later.';
            } else if (error.response && error.response.data && error.response.data.message) {
                errorMessage = `An API error occurred: ${error.response.data.message}`;
            } else {
                errorMessage = `An unexpected error occurred: ${error.message || String(error)}`;
            }

            const errorResponse = {
                id: Date.now() + 1,
                text: errorMessage,
                sender: 'ai',
                creationTimestamp: new Date().toISOString(),
            };
            setMessages((prevMessages) => [...prevMessages, errorResponse]);
            console.error("Error sending message:", error);
        } finally {
            setIsLoading(false); // Hide loading indicator (controlled by parent ContentPage)
        }
    };

    const handleInputChange = (e) => {
        setInput(e.target.value);
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !isLoading && input.trim() !== '') {
            handleSendMessage();
        }
    };

    return (
        <div className="flex flex-col h-full bg-white rounded-lg shadow-md overflow-hidden">
            {/* Chat Display Area */}
            <div
                ref={chatContainerRef}
                className="flex-1 min-h-0 overflow-y-auto p-4 space-y-4 break-words custom-scrollbar"
            >
                {messages.length === 0 && !isLoading ? ( // Only show empty state if not loading
                    <div className="text-center text-gray-500 py-10">
                        Tell me what kind of social media content you need!
                    </div>
                ) : (
                    messages.map((message) => (
                        <div
                            key={message.id || `${message.sender}-${message.creationTimestamp || Math.random()}`}
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
                                // Use dangerouslySetInnerHTML to render Markdown
                                dangerouslySetInnerHTML={{ __html: renderMarkdown(message.text) }}
                            />
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
                    disabled={isLoading}
                />
                <button
                    onClick={handleSendMessage}
                    className="ml-4 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                    disabled={isLoading || input.trim() === ''} // Disable if input is empty or loading
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
                /* Custom scrollbar styles */
                .custom-scrollbar {
                    /* For Webkit browsers (Chrome, Safari) */
                    &::-webkit-scrollbar {
                        width: 8px;
                    }

                    &::-webkit-scrollbar-track {
                        background: #f1f1f1;
                        border-radius: 10px;
                    }

                    &::-webkit-scrollbar-thumb {
                        background: #888;
                        border-radius: 10px;
                    }

                    &::-webkit-scrollbar-thumb:hover {
                        background: #555;
                    }

                    /* For Firefox */
                    scrollbar-width: thin; /* "auto" or "thin" */
                    scrollbar-color: #888 #f1f1f1; /* thumb and track color */
                }
            `}</style>
        </div>
    );
}

export default ChatInterface;