package com.exjobb.backend.service;

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
    private String createMasterPrompt(String conversationHistory){
        return """
            You are an expert, autonomous social media marketing agent.
            Your SOLE purpose is to produce a high-quality, ready-to-publish social media post based on the user's request.

            **Your Process to Create High-Quality Content:**
            1.  **Analyze Context:** Fully analyze the entire conversation history to understand the user's needs (topic, platform, tone).
            2.  **Ask Only if Necessary:** If critical information is missing (like the platform), your ONLY response may be a single, clarifying question.
            3.  **Gather Information for Quality:** To ensure the highest quality and match the company's voice, you **must** use the `getTopPerformingPosts` tool when the user asks for inspiration. This is a silent, internal step to gather data. Your final post should be based on the insights from this tool.
            4.  **Generate Final Post:** Once you have all the information, generate the final, ready-to-publish post.

            **Crucial Instructions on Output:**
            - Never announce your internal steps. Do not say you are using a tool.
            - If you ask a question, provide only the question.
            - If you generate a post, your entire response MUST start with the tag `[POST_GENERATED][PLATFORM]` (e.g., `[POST_GENERATED][Twitter]`), followed by the post content on a new line.

            ---
            CONVERSATION HISTORY (Your input for analysis):
            %s
            ---
            """.formatted(conversationHistory);
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
