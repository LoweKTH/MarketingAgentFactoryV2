import ConversationItem from "./ConversationItem";
import { Search } from 'lucide-react';

function ConversationList({ conversations, onSelectConversation, searchTerm }) {
    const filteredConversations = conversations.filter(conv =>
        conv.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        conv.lastMessage?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (filteredConversations.length === 0 && searchTerm) {
        return (
            <div className="text-center py-12">
                <Search className="w-12 h-12 text-gray-500 mx-auto mb-4" /> {/* Adjusted icon color */}
                <p className="text-gray-400">No conversations found matching "{searchTerm}"</p> {/* Adjusted text color */}
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {filteredConversations.map((conv) => (
                <ConversationItem
                    key={conv.id}
                    conversation={conv}
                    onSelect={onSelectConversation}
                />
            ))}
        </div>
    );
}
export default ConversationList;
