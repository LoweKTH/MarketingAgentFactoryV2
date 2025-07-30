// src/api/chatApi.js
import axiosInstance from './axiosInstance'; // Import your pre-configured Axios instance

// No longer needed, as axiosInstance.js handles this via an interceptor
// const getAuthHeaders = () => {
//     const token = localStorage.getItem('jwt_token'); // Or wherever you store your JWT
//     return token ? { 'Authorization': `Bearer ${token}` } : {};
// };

/**
 * Fetches chat messages for a specific conversation.
 * @param {number} conversationId - The ID of the conversation to fetch messages for.
 * @returns {Promise<Array<object>>} A promise that resolves to an array of message objects.
 * @throws {Error} If the API call fails or authentication is missing.
 */
export const fetchConversationMessages = async (conversationId) => {
    if (!conversationId) {
        throw new Error('Conversation ID is required to fetch messages.');
    }

    try {
        // Use axiosInstance.get()
        const response = await axiosInstance.get(`/chat/conversations/${conversationId}/messages`);

        // Axios automatically parses JSON into response.data
        console.log(response.data);
        return response.data; // This should be an array of your ChatMessage DTOs
    } catch (error) {
        console.error("Error in fetchConversationMessages:", error);
        // Rethrow the error for component-level handling, using Axios error structure
        throw error.response?.data?.message || error.message || "Failed to fetch messages";
    }
};

/**
 * Saves a generated social media post to the backend.
 * @param {string} content - The text content of the post.
 * @param {string} platform - The target social media platform.
 * @param {number|null} conversationId - The ID of the conversation that generated the post (optional).
 * @returns {Promise<object>} A promise that resolves to the saved SocialMediaPost object.
 * @throws {Error} If the API call fails or authentication is missing.
 */
export const saveSocialMediaPost = async (content, conversationId) => {
    try {
        // Use axiosInstance.post()
        const response = await axiosInstance.post('/social-media-posts', { // Note: baseURL already handles '/api'
            content: content,
            conversationId: conversationId // Pass current conversation ID if useful for linking posts
        });

        return response.data; // The saved SocialMediaPost entity
    } catch (error) {
        console.error("Error in saveSocialMediaPost:", error);
        // Rethrow the error for component-level handling, using Axios error structure
        throw error.response?.data?.message || error.message || "Failed to save social media post";
    }
};


/**
 * Fetches a list of all chat conversations for the current user.
 * @returns {Promise<Array<object>>} A promise that resolves to an array of conversation objects.
 * @throws {Error} If the API call fails or authentication is missing.
 */
export const fetchAllConversations = async () => {
    try {
        // Use axiosInstance.get()
        const response = await axiosInstance.get('/chat/conversations'); // Note: baseURL already handles '/api'

        // Axios automatically parses JSON into response.data
        const data = response.data;
        console.log("Fetched conversations:", data); // Debugging log
        return data; // This should be an array of your Conversation DTOs
    } catch (error) {
        console.error("Error in fetchAllConversations:", error);
        // Rethrow the error for component-level handling, using Axios error structure
        // The interceptor in axiosInstance.js already handles 401/403, but this catches other errors.
        throw error.response?.data?.message || error.message || "Failed to fetch conversations";
    }
};

/**
 * Sends a user message to the AI agent and gets a response, also handles saving to conversation.
 * This version sends only 'message' and 'conversationId' to match backend DTO.
 * @param {number|null} conversationId - The ID of the current conversation, or null if it's new.
 * @param {string} message - The text content of the user's message.
 * @returns {Promise<object>} A promise that resolves to the ChatMessageResponse object from backend.
 * (It contains agentResponse and potentially a new conversationId).
 * @throws {Error} If the API call fails.
 */
export const sendChatMessage = async (conversationId, message) => { // Removed platform, tone from parameters
    try {
        const response = await axiosInstance.post('/chat', {
            conversationId: conversationId,
            message: message, // Changed from userMessage to message to match backend DTO
            // platform and tone are no longer sent
        });
        return response.data;
    } catch (error) {
        console.error("Error in sendChatMessage:", error);
        throw error.response?.data?.message || error.message || "Failed to send message";
    }
};