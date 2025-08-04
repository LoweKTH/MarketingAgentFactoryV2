package com.exjobb.backend.service;

import com.exjobb.backend.dto.ChatConversationResponse;
import com.exjobb.backend.dto.ChatMessageRequest;
import com.exjobb.backend.dto.ChatMessageResponse;
import com.exjobb.backend.dto.PlanStep;
import com.exjobb.backend.entity.*;
import com.exjobb.backend.repository.ChatConversationRepository;
import com.exjobb.backend.repository.ChatMessageRepository;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ExternalDataToolService externalDataToolService;
    private final ObjectMapper objectMapper;

    public AgentService(@Qualifier("geminiChatClient") ChatClient chatClient,
            SocialMediaPostRepository postRepository,
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            InternalDataToolService internalDataToolService,
            ExternalDataToolService externalDataToolService,
            ObjectMapper objectMapper) {
        this.geminiChatClient = chatClient;
        this.postRepository = postRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.internalDataToolService = internalDataToolService;
        this.externalDataToolService = externalDataToolService;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes a non-interactive, standalone task for a given user.
     * This method does NOT create or save any chat messages. Its only goal is to
     * use the agent to produce and save a SocialMediaPost
     * 
     * @param prompt The instruction for the agent
     * @param user   The user context for the task.
     */
    public void executeStandaloneTask(String prompt, User user) {

        // Call planner to get json plan
        String plannerPrompt = createPlannerPrompt(prompt);
        logger.info("--- ORCHESTRATOR: Requesting plan from Planner AI for user '{}' ---", user.getUsername());
        String jsonPlanString = geminiChatClient.prompt().user(plannerPrompt).call().content();
        logger.info("--- ORCHESTRATOR: Received plan: {} ---", jsonPlanString);

        // Clean up Json
        String cleanedJson = jsonPlanString;
        if (cleanedJson.startsWith("```json")) {
            cleanedJson = cleanedJson.substring(7); // Tar bort ```json och en eventuell ny rad
        }
        if (cleanedJson.endsWith("```")) {
            cleanedJson = cleanedJson.substring(0, cleanedJson.lastIndexOf("```"));
        }
        cleanedJson = cleanedJson.trim();

        // Parse json plan
        List<PlanStep> plan;
        try {
            plan = objectMapper.readValue(cleanedJson, new TypeReference<List<PlanStep>>() {
            });
        } catch (JsonProcessingException e) {
            logger.error("--- ORCHESTRATOR: Failed to parse JSON plan from LLM. Aborting task.", e);
            return;
        }

        // Execute plan step by step
        Map<String, Object> executionContext = new HashMap<>();
        String finalResult = "Task finished with no specific results.";

        for (PlanStep step : plan) {
            logger.info("--- ORCHESTRATOR: Executing step -> {}", step.tool());
            try {
                switch (step.tool()) {
                    case "getMarketNews":
                        String news = externalDataToolService.getMarketNews(
                                step.parameters().get("topic"),
                                step.parameters().get("countryCode"));
                        executionContext.put("newsResult", news);
                        break;
                    case "getTopPerformingPosts":
                        String topPosts = internalDataToolService.getTopPerformingPosts();
                        executionContext.put("topPostsResult", topPosts);
                        break;
                    case "synthesizeText":
                        // The original goal from the planner, e.g., "Create a post about..."
                        String originalGoal = step.parameters().get("goal");

                        // A much stricter prompt for the synthesis step
                        String synthesisPrompt = """
                                Based on the following news: %s

                                And this inspiration: %s

                                Your task is to: %s

                                IMPORTANT: Your entire response MUST BE ONLY the text for the social media post.
                                Do not include any intro, preamble, or conversational text like "Here is the draft".
                                """.formatted(
                                executionContext.get("newsResult"),
                                executionContext.get("topPostsResult"),
                                originalGoal);

                        String generatedContent = geminiChatClient.prompt().user(synthesisPrompt).call().content();

                        // Add a call to a new cleanup method for extra safety
                        String cleanedContent = cleanPostContent(generatedContent);

                        executionContext.put("generatedContent", cleanedContent);
                        break;
                    // In your executeStandaloneTask method...

                    case "postToFacebook":
                        // This now gets the cleaned content
                        String contentToPost = (String) executionContext.get("generatedContent");

                        // Check if content is null or empty before posting
                        if (contentToPost == null || contentToPost.isBlank()) {
                            finalResult = "Skipped posting because generated content was empty.";
                            logger.warn("--- ORCHESTRATOR: " + finalResult);
                            break;
                        }

                        finalResult = externalDataToolService.postToFacebook(contentToPost, user);
                        break;

                     case "postToTwitter":
                        String contentToPostTw = (String) executionContext.get("generatedContent");
                         if (contentToPostTw == null || contentToPostTw.isBlank()) {
                            finalResult = "Skipped Twitter posting because generated content was empty.";
                            logger.warn("--- ORCHESTRATOR: " + finalResult);
                            break;
                        }
                        // Use the overloaded method that accepts a User object
                        finalResult = externalDataToolService.postToTwitter(contentToPostTw, user);
                        break;

                    default:
                        logger.warn("--- ORCHESTRATOR: Unknown tool in plan: {} ---", step.tool());
                }
            } catch (Exception e) {
                logger.error("--- ORCHESTRATOR: Failed to execute step {}. Aborting task. Error: {}", step.tool(),
                        e.getMessage());
                return;
            }
        }
        logger.info("--- ORCHESTRATOR: Plan execution finished with final result: {} ---", finalResult);
    }

    /**
     * Main method which handles all conversational logic with one single, powerful
     * prompt.
     * 
     * @param request
     * @param currentUser
     * @return
     */
    public ChatMessageResponse handleChatMessage(ChatMessageRequest request, User currentUser) {
        ChatConversation conversation = findOrCreateConversation(request, currentUser);
        saveMessage(request.message(), Role.USER, conversation);

        List<ChatMessage> history = messageRepository
                .findByConversationIdOrderByCreationTimeStampAsc(conversation.getId());
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

        saveMessage(agentResponse, Role.AGENT, conversation);
        return new ChatMessageResponse(agentResponse, conversation.getId());
    }

    /**
     * Retrieves all chat conversations for a given user ID, ordered by creation
     * timestamp.
     * 
     * @param userId The ID of the user for whom to retrieve conversations.
     * @return A list of ChatConversationResponse DTOs.
     */
    public List<ChatConversationResponse> getConversationsByUserId(Long userId) {
        // Now, directly use the new repository method to find by user ID
        return conversationRepository.findByUserIdOrderByCreationTimeStampDesc(userId).stream()
                .map(conversation -> new ChatConversationResponse(
                        conversation.getId(),
                        conversation.getTitle(),
                        conversation.getCreationTimeStamp()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all chat messages for a given conversation ID, ordered by creation
     * timestamp.
     * 
     * @param conversationId The ID of the conversation.
     * @return A list of ChatMessage entities.
     */
    public List<ChatMessage> getMessagesByConversationId(Long conversationId) {
        // You might want to add security check here if a user should only access their
        // own conversations
        // e.g., if (conversationRepository.findById(conversationId).map(c ->
        // !c.getUser().equals(currentUser)).orElse(true)) throw new
        // AccessDeniedException;
        return messageRepository.findByConversationIdOrderByCreationTimeStampAsc(conversationId);
    }

    /**
     * Creates the plan to be executed
     * 
     * @param userRequest
     * @return
     */
    private String createPlannerPrompt(String userRequest) {
        return """
                You are an intelligent planning agent. Your task is to take a user's request and create a step-by-step plan
                 to fulfill it using a set of available tools.
                    Return the plan as a JSON array of objects. Each object must have a 'tool' name and a 'parameters' object.

                    The available tools are:
                    - 'getMarketNews(topic, countryCode)'
                    - 'getTopPerformingPosts()'
                    - 'postToFacebook(content)'
                    - 'postToTwitter(content)'

                    If you need to generate text based on gathered information, use the special tool name 'synthesizeText'
                     and describe the goal in a 'goal' parameter.
                    The 'postToFacebook' and 'postToTwitter' tools requires content that must be generated by the 'synthesizeText' step first.

                    User Request: "%s"

                    JSON Plan:
                    """
                .formatted(userRequest);
    }

    /**
     * The interactive prompt that is sent to the LLM
     * 
     * @param conversationHistory
     * @return
     */
    private String createMasterPrompt(String conversationHistory) {
        return """
                You are an expert, helpful, and collaborative social media marketing agent. Your goal is to assist the user by creating content, providing ideas, and performing actions based on their instructions.

                **--- YOUR CORE BEHAVIOR ---**

                1.  **GATHER INFORMATION:** When the user asks for a post or ideas, use your information-gathering tools
                 (like `getMarketNews`, `getTopPerformingPosts`) silently to get the necessary context and inspiration.
                    - **TOPIC RULE:** If `getMarketNews` needs a `topic` and the user is general, you MUST use the default topic
                     `'business OR technology'`. DO NOT ask for a topic.

                2.  **HANDLE ACTION REQUESTS (VERY IMPORTANT):**
                    - Some of your tools perform irreversible actions that affect the outside world, like `postToFacebook`, `postToTwitter`, or `createTask`.
                    - **Before using an action tool, you MUST first present your generated content or your plan to the user and
                     ask for their explicit confirmation.**
                    - Example for posting: "Here is the draft I created based on the latest news: [the post content]. Shall I publish this to Facebook?"
                    - Example for posting to Twitter: "Here is the draft for Twitter: [the post content]. Ready to post it?"
                    - Example for scheduling: "I am ready to schedule a task to 'create a post about...'
                     every 12 hours. Is this correct?"
                    - Only after the user confirms with "yes", "okay", "go ahead" or similar, should you
                     call the action tool in your next turn.

                3.  **HANDLE INFORMATION REQUESTS:**
                    - If the user only asks for information (e.g., "what are the latest headlines?"),
                    provide the information directly and cleanly.

                ---
                CONVERSATION HISTORY:
                %s
                ---
                """
                .formatted(conversationHistory);
    }
    /**
     * Help method for database interaction - saves a creates a new- or finds a
     * current conversation
     * 
     * @param request
     * @param currentUser
     * @return
     */
    private ChatConversation findOrCreateConversation(ChatMessageRequest request, User currentUser) {
        if (request.conversationId() != null) {
            return conversationRepository.findById(request.conversationId())
                    .orElseThrow(
                            () -> new RuntimeException("Conversation not found with id: " + request.conversationId()));
        } else {
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
     * A private helper method for database interaction - Saves a message to the
     * database
     * 
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
     * A private helper method for database interaction - Saves a finalized social
     * media post to the database
     * * @param content The text content of the post generated by the agent.
     * * @param platform The target social media platform (e.g., "Twitter",
     * "Facebook").
     * * @param currentUser The user who initiated the creation of the post.
     * * @return The saved SocialMediaPost entity, now with its database-generated
     * ID.
     */
    private SocialMediaPost saveSocialMediaPost(String content, String platform, User currentUser) {
        logger.info("Saving generated post for platform '{}' to the database for user '{}'", platform,
                currentUser.getUsername());

        SocialMediaPost newPost = new SocialMediaPost();
        newPost.setContent(content);
        newPost.setPlatform(platform);
        newPost.setUser(currentUser);
        newPost.setEngagementScore(0.0);

        return this.postRepository.save(newPost);
    }

    /**
     * Removes common AI conversational preambles from generated text.
     * 
     * @param content The raw content from the AI
     * @return The cleaned content, ready for posting.
     */
    private String cleanPostContent(String content) {
        if (content == null) {
            return "";
        }
        // This regex looks for common starting phrases followed by a colon and optional
        // whitespace.
        // It will remove things like "Here's the post:", "Draft:", "Sure, here it is:",
        // etc.
        String cleaned = content.replaceAll("(?i)^.*?:\\s*", "").trim();
        return cleaned;
    }

}
