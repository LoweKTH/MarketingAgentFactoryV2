import React, { useState, useCallback } from 'react'; // Import useCallback
import ChatInterface from './components/ChatInterface.jsx';
import PreviewPanel from './components/PreviewPanel.jsx';
import ChatOptions from './components/ChatOptions.jsx';

/**
 * ContentPage Component (AI Agent Interface)
 * Manages the top-level layout for the AI chat and content preview.
 */
function ContentPage() {
    const [generatedContent, setGeneratedContent] = useState('');
    const [selectedPlatform, setSelectedPlatform] = useState('general');
    const [selectedTone, setSelectedTone] = useState('neutral');
    const [isLoading, setIsLoading] = useState(false);
    const [isPosting, setIsPosting] = useState(false); // New state for post button loading

    // Handler for the post button click
    const handlePostClick = useCallback(async () => {
        if (!generatedContent) {
            alert("No content to post yet!");
            return;
        }

        setIsPosting(true); // Set posting state to true
        console.log("Attempting to post content:", generatedContent);

        // Simulate an API call
        try {
            // Replace with your actual API call (e.g., fetch, axios)
            await new Promise(resolve => setTimeout(resolve, 2000)); // Simulate 2-second API delay
            console.log("Content successfully posted!");
            alert("Content successfully posted!"); // User feedback
            // You might want to clear generatedContent or take other actions after posting
            // setGeneratedContent('');
        } catch (error) {
            console.error("Error posting content:", error);
            alert("Failed to post content. Please try again.");
        } finally {
            setIsPosting(false); // Reset posting state
        }
    }, [generatedContent]); // Recreate if generatedContent changes

    return (
        <div className="flex w-full h-[calc(100vh-64px)]">

            {/* Left Column: AI Chat Interface and Options */}
            <div className="w-1/2 border-r border-gray-200 flex flex-col px-4 py-2">
                <ChatOptions
                    selectedPlatform={selectedPlatform}
                    onPlatformChange={(e) => setSelectedPlatform(e.target.value)}
                    selectedTone={selectedTone}
                    onToneChange={(e) => setSelectedTone(e.target.value)}
                    isDisabled={isLoading || isPosting} // Disable options when loading AI or posting
                />

                <ChatInterface
                    onContentGenerated={setGeneratedContent}
                    selectedPlatform={selectedPlatform}
                    selectedTone={selectedTone}
                    isLoading={isLoading}
                    setIsLoading={setIsLoading}
                />
            </div>

            {/* Right Column: Content Preview */}
            <div className="w-1/2 flex flex-col bg-gray-100 p-4">
                {/* Pass onPostClick and isPosting to PreviewPanel */}
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