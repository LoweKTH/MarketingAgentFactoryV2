import React, { useState, useCallback, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ChatInterface from './components/ChatInterface.jsx';
import PreviewPanel from './components/PreviewPanel.jsx';
import ChatOptions from './components/ChatOptions.jsx';
// Import your API functions
import { fetchConversationMessages, saveSocialMediaPost } from '../../api/chatApi.js';

/**
 * ContentPage Component (AI Agent Interface)
 * Manages the top-level layout for the AI chat and content preview.
 */
function ContentPage() {
    const { conversationId: routeConversationId } = useParams();
    const [generatedContent, setGeneratedContent] = useState('');
    const [selectedPlatform, setSelectedPlatform] = useState('general');
    const [selectedTone, setSelectedTone] = useState('neutral');
    const [isLoading, setIsLoading] = useState(false);
    const [isPosting, setIsPosting] = useState(false);
    const [currentConversationId, setCurrentConversationId] = useState(null);
    const [initialMessages, setInitialMessages] = useState([]);
    const [messagesLoading, setMessagesLoading] = useState(true);

    // Effect to set the current conversation ID when the route parameter changes
    useEffect(() => {
        setCurrentConversationId(routeConversationId ? parseInt(routeConversationId, 10) : null);
    }, [routeConversationId]);

    // Effect to fetch initial messages when an existing conversation is loaded
    useEffect(() => {
        if (currentConversationId) {
            const loadMessages = async () => { // Renamed to avoid conflict with imported fetchMessages
                setMessagesLoading(true);
                try {
                    // *** Use your imported API function here ***
                    const data = await fetchConversationMessages(currentConversationId);
                    setInitialMessages(data);
                } catch (error) {
                    console.error("Error fetching initial messages:", error);
                    // You might want to display an error message to the user here
                    setInitialMessages([]); // Clear messages on error
                } finally {
                    setMessagesLoading(false);
                }
            };
            loadMessages(); // Call the async function
        } else {
            // If it's a new conversation, clear any old initial messages
            setInitialMessages([]);
            setMessagesLoading(false); // No messages to load for a new conversation
        }
    }, [currentConversationId]); // Re-run when currentConversationId changes

    // Handler for the post button click
    const handlePostClick = useCallback(async () => {
        if (!generatedContent) {
            alert("No content to post yet!");
            return;
        }

        setIsPosting(true);
        console.log("Attempting to post content:", generatedContent);

        try {
            // *** Use your imported API function here ***
            await saveSocialMediaPost(generatedContent, selectedPlatform, currentConversationId);

            console.log("Content successfully posted!");
            alert("Content successfully posted!");
            setGeneratedContent(''); // Clear generated content after posting
            // Optionally, you could refetch conversations or messages here if needed
        } catch (error) {
            console.error("Error posting content:", error);
            alert("Failed to post content. Please try again.");
        } finally {
            setIsPosting(false);
        }
    }, [generatedContent, selectedPlatform, currentConversationId]);

    if (messagesLoading) {
        return (
            <div className="flex justify-center items-center h-[calc(100vh-64px)]">
                <p>Loading chat history...</p>
            </div>
        );
    }

    return (
        <div className="flex w-full h-[calc(100vh-64px)]">
            {/* Left Column: AI Chat Interface and Options */}
            <div className="w-1/2 border-r border-gray-200 flex flex-col px-4 py-2">
                <ChatOptions
                    selectedPlatform={selectedPlatform}
                    onPlatformChange={(e) => setSelectedPlatform(e.target.value)}
                    selectedTone={selectedTone}
                    onToneChange={(e) => setSelectedTone(e.target.value)}
                    isDisabled={isLoading || isPosting}
                />

                <ChatInterface
                    onContentGenerated={setGeneratedContent}
                    selectedPlatform={selectedPlatform}
                    selectedTone={selectedTone}
                    isLoading={isLoading}
                    setIsLoading={setIsLoading}
                    conversationId={currentConversationId}
                    initialMessages={initialMessages}
                    setCurrentConversationId={setCurrentConversationId}
                />
            </div>

            {/* Right Column: Content Preview */}
            <div className="w-1/2 flex flex-col bg-gray-100 p-4">
                <PreviewPanel
                    content={generatedContent}
                    onPostClick={handlePostClick}
                    isPosting={isPosting}
                />
            </div>
        </div>
    );
}

export default ContentPage;