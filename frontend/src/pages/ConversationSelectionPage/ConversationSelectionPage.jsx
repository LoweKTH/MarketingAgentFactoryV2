import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchAllConversations } from '../../api/chatApi';
import ConversationList from './components/ConversationList';
import { Search, Plus, MessageCircle, Loader2, AlertCircle } from 'lucide-react';

function ConversationSelectionPage() {
    const [conversations, setConversations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const loadConversations = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await fetchAllConversations();
                setConversations(data);
            } catch (err) {
                console.error("Error fetching conversations:", err);
                setError(`Failed to load conversations: ${err.message}. Please try again.`);
            } finally {
                setLoading(false);
            }
        };

        loadConversations();
    }, []);

    const handleSelectConversation = (conversationId) => {
        navigate(`/chat/${conversationId}`);
    };

    const handleNewConversation = () => {
        navigate('/chat');
    };

    const handleRetry = () => {
        const loadConversations = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await fetchAllConversations();
                setConversations(data);
            } catch (err) {
                console.error("Error fetching conversations:", err);
                setError(`Failed to load conversations: ${err.message}. Please try again.`);
            } finally {
                setLoading(false);
            }
        };
        loadConversations();
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-950 flex items-center justify-center"> {/* Darker background */}
                <div className="text-center">
                    <Loader2 className="w-12 h-12 text-indigo-500 animate-spin mx-auto mb-4" /> {/* Adjusted loader color */}
                    <p className="text-gray-300 text-lg">Loading your conversations...</p> {/* Adjusted text color */}
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-gray-950 flex items-center justify-center"> {/* Darker background */}
                <div className="bg-gray-800 rounded-xl shadow-lg p-8 max-w-md text-center"> {/* Darker card background */}
                    <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
                    <h2 className="text-xl font-semibold text-white mb-2">Oops! Something went wrong</h2> {/* Adjusted text color */}
                    <p className="text-gray-300 mb-6">{error}</p> {/* Adjusted text color */}
                    <button
                        onClick={handleRetry}
                        className="bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 px-6 rounded-lg transition-colors"
                    >
                        Try Again
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-950 dark:to-blue-950 font-sans"> {/* Dark gradient background */}
            <div className="container mx-auto px-6 py-12 max-w-4xl">
                {/* Header Section */}
                <div className="text-center mb-12">
                    <h1 className="text-4xl font-bold text-white mb-4"> {/* Adjusted text color */}
                        Your Conversations
                    </h1>
                    <p className="text-gray-300 text-lg"> {/* Adjusted text color */}
                        Continue where you left off or start something new
                    </p>
                </div>

                {/* New Conversation Button */}
                <button
                    onClick={handleNewConversation}
                    className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white font-semibold py-4 px-6 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 ease-in-out mb-8 text-lg flex items-center justify-center gap-3 transform hover:-translate-y-1"
                >
                    <Plus className="w-6 h-6" />
                    Start a New Conversation
                </button>

                {/* Search Bar */}
                {conversations.length > 0 && (
                    <div className="relative mb-8">
                        <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                        <input
                            type="text"
                            placeholder="Search conversations..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-12 pr-4 py-4 bg-gray-700 border border-gray-600 rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-white placeholder-gray-400"
                        />
                    </div>
                )}

                {/* Conversations List */}
                {conversations.length === 0 ? (
                    <div className="bg-gray-800 rounded-xl shadow-sm p-12 text-center"> {/* Darker card background */}
                        <MessageCircle className="w-16 h-16 text-gray-600 mx-auto mb-6" /> {/* Adjusted icon color */}
                        <h2 className="text-2xl font-semibold text-white mb-3"> {/* Adjusted text color */}
                            No conversations yet
                        </h2>
                        <p className="text-gray-300 text-lg mb-8"> {/* Adjusted text color */}
                            Start your first conversation and it will appear here
                        </p>
                        <button
                            onClick={handleNewConversation}
                            className="bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-3 px-8 rounded-lg transition-colors inline-flex items-center gap-2"
                        >
                            <Plus className="w-5 h-5" />
                            Get Started
                        </button>
                    </div>
                ) : (
                    <div className="bg-gray-800 rounded-xl shadow-sm p-6"> {/* Darker card background */}
                        <ConversationList
                            conversations={conversations}
                            onSelectConversation={handleSelectConversation}
                            searchTerm={searchTerm}
                        />
                    </div>
                )}
            </div>
        </div>
    );
}

export default ConversationSelectionPage;
