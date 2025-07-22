// src/api/agentApi.js
const API_BASE_URL = 'http://localhost:8080/api/agent'; // Replace with your backend URL

/**
 * Generate content plan by calling the backend AI agent
 * @param {object} contentRequest - The content request object
 * @param {string} contentRequest.topic - The main topic/subject for the content generation.
 * @param {string} contentRequest.targetPlatform - The social media platform for which the content is intended.
 * @param {string} [contentRequest.extraInstructions=''] - Optional additional instructions for the AI agent.
 * @returns {Promise<object>} - Returns SocialMediaPost object from backend
 */
export const generateContentPlan = async (contentRequest) => {
    try {
        // Get JWT token from localStorage
        const token = localStorage.getItem('jwtToken'); // Match your auth token key

        if (!token) {
            throw new Error('No authentication token found. Please login first.');
        }

        const response = await fetch(`${API_BASE_URL}/generate-plan`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            body: JSON.stringify(contentRequest),
        });

        const data = await response.json();

        if (!response.ok) {
            // Handle different error status codes
            if (response.status === 401) {
                throw new Error('Authentication failed. Please login again.');
            } else if (response.status === 403) {
                throw new Error('Access denied. You do not have permission to generate content.');
            } else if (response.status === 500) {
                throw new Error('Server error. Please try again later.');
            } else {
                throw new Error(data.message || 'Failed to generate content plan');
            }
        }

        return data; // Returns SocialMediaPost object
    } catch (error) {
        console.error('API generate content plan error:', error);
        throw error;
    }
};

/**
 * Creates a content request object for the backend API.
 * @param {string} topic - The main topic for the content generation.
 * @param {string} targetPlatform - The target social media platform (e.g., 'Instagram', 'LinkedIn').
 * @param {string} [extraInstructions=''] - Optional extra instructions for the AI.
 * @returns {object} The ContentRequest DTO ready for the backend.
 */
export const createContentRequest = (topic, targetPlatform, extraInstructions = '') => {
    return {
        topic: topic,
        targetPlatform: targetPlatform,
        extraInstructions: extraInstructions
    };
};

/**
 * Extract content from SocialMediaPost response for display
 * @param {object} socialMediaPost - The SocialMediaPost object from backend
 * @returns {string} - Formatted content for display
 */
export const extractContentFromResponse = (socialMediaPost) => {
    // Adjust these field names based on your actual SocialMediaPost entity structure
    return socialMediaPost.content ||
        socialMediaPost.message ||
        socialMediaPost.text ||
        socialMediaPost.postContent ||
        'Generated content from AI';
};

/**
 * Check if user is authenticated (has valid token)
 * @returns {boolean} - True if user has a token
 */
export const isAuthenticated = () => {
    const token = localStorage.getItem('jwtToken');
    return token !== null && token !== undefined;
};