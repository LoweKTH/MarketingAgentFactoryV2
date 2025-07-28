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
            <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
                <div className="text-center">
                    <Loader2 className="w-12 h-12 text-blue-600 animate-spin mx-auto mb-4" />
                    <p className="text-gray-600 text-lg">Loading your conversations...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
                <div className="bg-white rounded-xl shadow-lg p-8 max-w-md text-center">
                    <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
                    <h2 className="text-xl font-semibold text-gray-900 mb-2">Oops! Something went wrong</h2>
                    <p className="text-gray-600 mb-6">{error}</p>
                    <button
                        onClick={handleRetry}
                        className="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-6 rounded-lg transition-colors"
                    >
                        Try Again
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
            <div className="container mx-auto px-6 py-12 max-w-4xl">
                {/* Header Section */}
                <div className="text-center mb-12">
                    <h1 className="text-4xl font-bold text-gray-900 mb-4">
                        Your Conversations
                    </h1>
                    <p className="text-gray-600 text-lg">
                        Continue where you left off or start something new
                    </p>
                </div>

                {/* New Conversation Button */}
                <button
                    onClick={handleNewConversation}
                    className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold py-4 px-6 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 ease-in-out mb-8 text-lg flex items-center justify-center gap-3 transform hover:-translate-y-1"
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
                            className="w-full pl-12 pr-4 py-4 bg-white border border-gray-200 rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 placeholder-gray-500"
                        />
                    </div>
                )}

                {/* Conversations List */}
                {conversations.length === 0 ? (
                    <div className="bg-white rounded-xl shadow-sm p-12 text-center">
                        <MessageCircle className="w-16 h-16 text-gray-300 mx-auto mb-6" />
                        <h2 className="text-2xl font-semibold text-gray-900 mb-3">
                            No conversations yet
                        </h2>
                        <p className="text-gray-600 text-lg mb-8">
                            Start your first conversation and it will appear here
                        </p>
                        <button
                            onClick={handleNewConversation}
                            className="bg-blue-600 hover:bg-blue-700 text-white font-medium py-3 px-8 rounded-lg transition-colors inline-flex items-center gap-2"
                        >
                            <Plus className="w-5 h-5" />
                            Get Started
                        </button>
                    </div>
                ) : (
                    <div className="bg-white rounded-xl shadow-sm p-6">
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