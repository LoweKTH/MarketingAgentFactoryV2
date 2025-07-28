package com.exjobb.backend.service;

import com.exjobb.backend.dto.ChatConversationResponse;
import com.exjobb.backend.dto.ChatMessageRequest;
import com.exjobb.backend.dto.ChatMessageResponse;
import com.exjobb.backend.entity.*;
import com.exjobb.backend.repository.ChatConversationRepository;
import com.exjobb.backend.repository.ChatMessageRepository;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AgentService {


    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    private final ChatClient geminiChatClient;
    private final SocialMediaPostRepository postRepository;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final InternalDataToolService internalDataToolService;


    public AgentService(@Qualifier("geminiChatClient") ChatClient chatClient,
                        SocialMediaPostRepository postRepository,
                        ChatConversationRepository conversationRepository,
                        ChatMessageRepository messageRepository,
                        InternalDataToolService internalDataToolService) {
        this.geminiChatClient = chatClient;
        this.postRepository = postRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.internalDataToolService = internalDataToolService;
    }

    /**
     * Main method which handles all conversational logic with one single, powerful prompt.
     * @param request
     * @param currentUser
     * @return
     */
    public ChatMessageResponse handleChatMessage(ChatMessageRequest request, User currentUser) {
        ChatConversation conversation = findOrCreateConversation(request, currentUser);
        saveMessage(request.message(), Role.USER, conversation);

        List<ChatMessage> history = messageRepository.findByConversationIdOrderByCreationTimeStampAsc(conversation.getId());
        String historyString = history.stream()
                .map(msg -> msg.getRole().name() + ": " + msg.getMessage())
                .collect(Collectors.joining("\n"));

        String masterPrompt = createMasterPrompt(historyString);

        logger.info("--- SENDING MASTER PROMPT TO LLM ---");
        String agentResponse = this.geminiChatClient.prompt()
                .user(masterPrompt)
                .call()
                .content();
        logger.info("--- RESPONSE RECEIVED FROM LLM ---");

        String finalResponseForUser = agentResponse;
        String trimmedResponse = agentResponse.trim();

        final String postTag = "[POST_GENERATED]";
        if(trimmedResponse.startsWith(postTag)){
            logger.info("Agent generated a final post. Processing and saving to database.");

            Pattern pattern = Pattern.compile("\\[POST_GENERATED\\]\\[(.*?)\\]");
            Matcher matcher = pattern.matcher(agentResponse);

            if(matcher.find()){
                String platform = matcher.group(1); // Extracting platform, i.e. twitter
                String content = agentResponse.substring(matcher.end()).trim();

                saveSocialMediaPost(content, platform, currentUser);

                finalResponseForUser = content;
            }else{
                logger.warn("Agent used the POST_GENERATED tag but the format was incorrect. Not saving post.");
            }

        }

        saveMessage(finalResponseForUser, Role.AGENT, conversation);
        return new ChatMessageResponse(agentResponse, conversation.getId());
    }

    /**
     * THE prompt that is being sent to the LLM
     * @param conversationHistory
     * @return
     */
    // I AgentService.java

    private String createMasterPrompt(String conversationHistory) {
        return """
            You are an expert, autonomous social media marketing agent. Your goal is to fulfill the user's request by using your tools and generating content.

            **--- YOUR DECISION PROCESS ---**

            1.  **ANALYZE THE USER'S LATEST REQUEST.**
                - Does the user want to **schedule a recurring task** (e.g., "every day", "every 12 hours")?
                 If YES, your primary goal is to call the `createTask` tool. Proceed to step 2 to determine the parameters for that tool.
                - Does the user want a **finished post right now**?
                 If YES, your goal is to generate content with the `[POST_GENERATED]` tag. Proceed to step 2.
                - Does the user want **information or ideas**? If YES, your goal is to provide a clean text answer. Proceed to step 2.

            2.  **GATHER INFORMATION FOR THE GOAL.**
                - To fulfill your goal, you MUST use your available tools.
                - If the user's request involves "news", you MUST use the `getMarketNews` tool.
                - **TOPIC RULE:** If the `getMarketNews` tool requires a `topic` and the user's request is general (e.g., "latest news"),
                 you **MUST** use the default topic `'business OR technology'`. **DO NOT ask the user for a topic.**
                - If the user's request mentions "tone" or "inspiration", you should also use the `getTopPerformingPosts` tool.

            3.  **EXECUTE.**
                - If your goal was to schedule a task, call the `createTask` tool now with the prompt and schedule you have determined.
                - If your goal was to create a post or provide information,
                 generate the final response now using the information you gathered from your tools.

            **--- OUTPUT FORMATTING ---**
            - For informational requests, provide clean text.
            - For post creation requests, use the `[POST_GENERATED][PLATFORM]` tag.
            - When you call the `createTask` tool, your final response to the user should be the confirmation message from that tool.

            ---
            CONVERSATION HISTORY:
            %s
            ---
            """.formatted(conversationHistory);
    }



    /**
     * Retrieves all chat conversations for a given user ID, ordered by creation timestamp.
     * @param userId The ID of the user for whom to retrieve conversations.
     * @return A list of ChatConversationResponse DTOs.
     */
    public List<ChatConversationResponse> getConversationsByUserId(Long userId) {
        // Now, directly use the new repository method to find by user ID
        return conversationRepository.findByUserIdOrderByCreationTimeStampDesc(userId).stream()
                .map(conversation -> new ChatConversationResponse(
                        conversation.getId(),
                        conversation.getTitle(),
                        conversation.getCreationTimeStamp()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all chat messages for a given conversation ID, ordered by creation timestamp.
     * @param conversationId The ID of the conversation.
     * @return A list of ChatMessage entities.
     */
    public List<ChatMessage> getMessagesByConversationId(Long conversationId) {
        // You might want to add security check here if a user should only access their own conversations
        // e.g., if (conversationRepository.findById(conversationId).map(c -> !c.getUser().equals(currentUser)).orElse(true)) throw new AccessDeniedException;
        return messageRepository.findByConversationIdOrderByCreationTimeStampAsc(conversationId);
    }



    /**
     * Help method for database interaction - saves a creates a new- or finds a current conversation
     * @param request
     * @param currentUser
     * @return
     */
    private ChatConversation findOrCreateConversation(ChatMessageRequest request, User currentUser) {
        if(request.conversationId() != null){
            return conversationRepository.findById(request.conversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + request.conversationId()));
        }else{
            ChatConversation conversation = new ChatConversation();
            conversation.setUser(currentUser);
            // Setting temporary title, can be updated later
            conversation.setTitle(request.message().substring(0, Math.min(request.message().length(), 50)));
            ChatConversation savedConversation = conversationRepository.save(conversation);
            logger.info("Created new conversation with id {}", savedConversation.getId());
            return savedConversation;
        }
    }

    /**
     * A private helper method for database interaction - Saves a message to the database
     * @param message
     * @param role
     * @param conversation
     */
    private void saveMessage(String message, Role role, ChatConversation conversation) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(conversation);
        chatMessage.setMessage(message);
        chatMessage.setRole(role);
        messageRepository.save(chatMessage);
        logger.info("Saved {} message to conversation {}", role, conversation.getId());
    }

    /**
     * A private helper method for database interaction - Saves a finalized social media post to the database
     *  * @param content The text content of the post generated by the agent.
     *  * @param platform The target social media platform (e.g., "Twitter", "Facebook").
     *  * @param currentUser The user who initiated the creation of the post.
     *  * @return The saved SocialMediaPost entity, now with its database-generated ID.
     */
    private SocialMediaPost saveSocialMediaPost(String content, String platform, User currentUser){
        logger.info("Saving generated post for platform '{}' to the database for user '{}'", platform, currentUser.getUsername());

        SocialMediaPost newPost = new SocialMediaPost();
        newPost.setContent(content);
        newPost.setPlatform(platform);
        newPost.setUser(currentUser);
        newPost.setEngagementScore(0.0);

        return this.postRepository.save(newPost);
    }


}
