import React, { useState, useCallback, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ChatInterface from './components/ChatInterface.jsx';
import PreviewPanel from './components/PreviewPanel.jsx';
import SocialConnections from './components/SocialConnections.jsx';

// Import your API functions
import { fetchConversationMessages, saveSocialMediaPost } from '../../api/chatApi.js';
import { getFacebookStatus, redirectToFacebookOAuth, redirectToTwitterOAuth, getTwitterStatus } from '../../api/authApi.js';

/**
 * ContentPage Component (AI Agent Interface)
 * Manages the top-level layout for the AI chat and content preview.
 */
function ContentPage() {
    const { conversationId: routeConversationId } = useParams();
    const [generatedContent, setGeneratedContent] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [isPosting, setIsPosting] = useState(false);
    const [currentConversationId, setCurrentConversationId] = useState(null);
    const [initialMessages, setInitialMessages] = useState([]);
    const [messagesLoading, setMessagesLoading] = useState(true);

    // Social media connection states
    const [fbConnected, setFbConnected] = useState(false);
    const [fbStatusLoading, setFbStatusLoading] = useState(false);
    const [twConnected, setTwConnected] = useState(false);
    const [twStatusLoading, setTwStatusLoading] = useState(false);

    // Effect to set the current conversation ID when the route parameter changes
    useEffect(() => {
        setCurrentConversationId(routeConversationId ? parseInt(routeConversationId, 10) : null);
    }, [routeConversationId]);

    // Effect to fetch initial messages when an existing conversation is loaded
    useEffect(() => {
        if (currentConversationId) {
            const loadMessages = async () => {
                setMessagesLoading(true);
                try {
                    const data = await fetchConversationMessages(currentConversationId);
                    setInitialMessages(data);
                } catch (error) {
                    console.error("Error fetching initial messages:", error);
                    setInitialMessages([]);
                } finally {
                    setMessagesLoading(false);
                }
            };
            loadMessages();
        } else {
            setInitialMessages([]);
            setMessagesLoading(false);
        }
    }, [currentConversationId]);

    // Check social media connection status on component mount
    useEffect(() => {
        const fetchFacebookStatus = async () => {
            setFbStatusLoading(true);
            try {
                const data = await getFacebookStatus();
                setFbConnected(!!data.connected);
            } catch (e) {
                setFbConnected(false);
            } finally {
                setFbStatusLoading(false);
            }
        };

        const fetchTwitterStatus = async () => {
            setTwStatusLoading(true);
            try {
                const data = await getTwitterStatus();
                setTwConnected(!!data.connected);
            } catch (e) {
                setTwConnected(false);
            } finally {
                setTwStatusLoading(false);
            }
        };

        fetchFacebookStatus();
        fetchTwitterStatus();
    }, []);

    // Social media connection handlers
    const handleConnectFacebook = () => {
        redirectToFacebookOAuth();
    };

    const handleConnectTwitter = () => {
        redirectToTwitterOAuth();
    };

    // Handler for the post button click
    const handlePostClick = useCallback(async () => {
        if (!generatedContent) {
            alert("No content to post yet!");
            return;
        }

        setIsPosting(true);
        console.log("Attempting to post content:", generatedContent);

        try {
            await saveSocialMediaPost(generatedContent, currentConversationId);
            console.log("Content successfully posted!");
            alert("Content successfully posted!");
            setGeneratedContent('');
        } catch (error) {
            console.error("Error posting content:", error);
            alert("Failed to post content. Please try again.");
        } finally {
            setIsPosting(false);
        }
    }, [generatedContent, currentConversationId]);

    if (messagesLoading) {
        return (
            <div className="flex justify-center items-center h-[calc(100vh-64px)] bg-gray-950 text-gray-300">
                <p>Loading chat history...</p>
            </div>
        );
    }

    return (
        <div className="flex w-full h-[calc(100vh-64px)] bg-gradient-to-br from-gray-950 to-gray-800">
            {/* Left Column: AI Chat Interface and Social Connections */}
            <div className="w-1/2 border-r border-gray-700 flex flex-col bg-gradient-to-l from-blue-950 to-gray-950">


                {/* Chat Interface */}
                <div className="flex-1 px-4 py-2 overflow-y-auto">
                    <ChatInterface
                        onContentGenerated={setGeneratedContent}
                        isLoading={isLoading}
                        setIsLoading={setIsLoading}
                        conversationId={currentConversationId}
                        initialMessages={initialMessages}
                        setCurrentConversationId={setCurrentConversationId}
                    />
                </div>
            </div>

            {/* Right Column: Content Preview */}
            <div className="w-1/2 flex flex-col bg-gradient-to-l from-gray-950 to-blue-950 p-4">
                <PreviewPanel
                    content={generatedContent}
                    onPostClick={handlePostClick}
                    isPosting={isPosting}
                />
                {/* Social Connections Section */}
                <SocialConnections
                    fbConnected={fbConnected}
                    fbStatusLoading={fbStatusLoading}
                    onConnectFacebook={handleConnectFacebook}
                    twConnected={twConnected}
                    twStatusLoading={twStatusLoading}
                    onConnectTwitter={handleConnectTwitter}
                />
            </div>
        </div>
    );
}

export default ContentPage;