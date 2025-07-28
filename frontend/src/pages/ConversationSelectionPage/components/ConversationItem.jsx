import React from 'react';
import { MessageCircle, Clock, ChevronRight } from 'lucide-react';

function ConversationItem({ conversation, onSelect }) {
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const now = new Date();

        // Remove time info to compare only dates
        const dateOnly = new Date(date.getFullYear(), date.getMonth(), date.getDate());
        const nowOnly = new Date(now.getFullYear(), now.getMonth(), now.getDate());

        const diffTime = nowOnly - dateOnly;
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 0) return 'Today';
        if (diffDays === 1) return 'Yesterday';
        if (diffDays < 7) return `${diffDays} days ago`;
        return date.toLocaleDateString();
    };

    return (
        <div
            onClick={() => onSelect(conversation.id)}
            className="group bg-white border border-gray-200 rounded-xl p-6 shadow-sm hover:shadow-lg hover:border-blue-300 transition-all duration-200 ease-in-out cursor-pointer transform hover:-translate-y-1"
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onSelect(conversation.id);
                }
            }}
        >
            <div className="flex items-start justify-between">
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-3 mb-2">
                        <div className="p-2 bg-blue-50 rounded-lg group-hover:bg-blue-100 transition-colors">
                            <MessageCircle className="w-5 h-5 text-blue-600" />
                        </div>
                        <h2 className="text-lg font-semibold text-gray-900 truncate">
                            {conversation.title || `Conversation ${conversation.id}`}
                        </h2>
                    </div>

                    {conversation.lastMessage && (
                        <p className="text-sm text-gray-600 mb-3 line-clamp-2 pl-11">
                            {conversation.lastMessage}
                        </p>
                    )}

                    <div className="flex items-center gap-4 text-xs text-gray-500 pl-11">
                        <div className="flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            <span>{formatDate(conversation.creationTimeStamp)}</span>
                        </div>
                        {conversation.messageCount && (
                            <span className="bg-gray-100 px-2 py-1 rounded-full">
                                {conversation.messageCount} messages
                            </span>
                        )}
                    </div>
                </div>

                <ChevronRight className="w-5 h-5 text-gray-400 group-hover:text-blue-600 transition-colors flex-shrink-0 mt-1" />
            </div>
        </div>
    );
}
export default ConversationItem;