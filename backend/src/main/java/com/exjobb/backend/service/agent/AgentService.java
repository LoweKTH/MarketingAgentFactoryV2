package com.exjobb.backend.service.agent;

import com.exjobb.backend.dto.ChatConversationResponse;
import com.exjobb.backend.dto.ChatMessageRequest;
import com.exjobb.backend.dto.ChatMessageResponse;
import com.exjobb.backend.dto.PlanStep;
import com.exjobb.backend.entity.*;
import com.exjobb.backend.repository.ChatConversationRepository;
import com.exjobb.backend.repository.ChatMessageRepository;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import com.exjobb.backend.service.mcptools.ExternalDataToolService;
import com.exjobb.backend.service.mcptools.InternalDataToolService;
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

        String plannerPrompt = createPlannerPrompt(prompt);
        logger.info("ORCHESTRATOR: Requesting plan from Planner AI for user '{}'", user.getUsername());
        String jsonPlanString = geminiChatClient.prompt().user(plannerPrompt).call().content();
        logger.info("ORCHESTRATOR: Received plan: {}", jsonPlanString);

        String cleanedJson = jsonPlanString;
        if (cleanedJson.startsWith("```json")) {
            cleanedJson = cleanedJson.substring(7);
        }
        if (cleanedJson.endsWith("```")) {
            cleanedJson = cleanedJson.substring(0, cleanedJson.lastIndexOf("```"));
        }
        cleanedJson = cleanedJson.trim();

        List<PlanStep> plan;
        try {
            plan = objectMapper.readValue(cleanedJson, new TypeReference<List<PlanStep>>() {
            });
        } catch (JsonProcessingException e) {
            logger.error("ORCHESTRATOR: Failed to parse JSON plan from LLM. Aborting task.", e);
            return;
        }

        Map<String, Object> executionContext = new HashMap<>();
        String finalResult = "Task finished with no specific results.";

        for (PlanStep step : plan) {
            logger.info("ORCHESTRATOR: Executing step -> {}", step.tool());
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
                        String originalGoal = step.parameters().get("goal");
                        String platform = step.parameters().get("platform");

                        String synthesisPrompt = createSynthesisPrompt(
                                (String) executionContext.get("newsResult"),
                                (String) executionContext.get("topPostsResult"),
                                originalGoal,
                                platform);

                        String generatedContent = geminiChatClient.prompt().user(synthesisPrompt).call().content();
                        String cleanedContent = cleanPostContent(generatedContent);

                        executionContext.put("generatedContent", cleanedContent);
                        break;


                    case "postToFacebook":
                        String contentToPost = (String) executionContext.get("generatedContent");

                        if (contentToPost == null || contentToPost.isBlank()) {
                            finalResult = "Skipped posting because generated content was empty.";
                            logger.warn("ORCHESTRATOR: " + finalResult);
                            break;
                        }

                        finalResult = externalDataToolService.postToFacebook(contentToPost, user);
                        break;

                    case "postToTwitter":
                        String contentToPostTw = (String) executionContext.get("generatedContent");
                        if (contentToPostTw == null || contentToPostTw.isBlank()) {
                            finalResult = "Skipped Twitter posting because generated content was empty.";
                            logger.warn("ORCHESTRATOR: " + finalResult);
                            break;
                        }
                        // Use the overloaded method that accepts a User object
                        finalResult = externalDataToolService.postToTwitter(contentToPostTw, user);
                        break;

                    default:
                        logger.warn("ORCHESTRATOR: Unknown tool in plan: {}", step.tool());
                }
            } catch (Exception e) {
                logger.error("ORCHESTRATOR: Failed to execute step {}. Aborting task. Error: {}", step.tool(),
                        e.getMessage());
                return;
            }
        }
        logger.info("ORCHESTRATOR: Plan execution finished with final result: {}", finalResult);
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

        logger.info("SENDING MASTER PROMPT TO LLM");
        String agentResponse = this.geminiChatClient.prompt()
                .user(masterPrompt)
                .call()
                .content();
        logger.info("RESPONSE RECEIVED FROM LLM");

        final String imagePrefix = "IMAGE_RESULT:::";
        String textResponseForUser = agentResponse;
        String imageDataForFrontend = null;

        if (agentResponse != null && agentResponse.startsWith(imagePrefix)) {
            imageDataForFrontend = agentResponse.substring(imagePrefix.length()).trim();

            textResponseForUser = "Here is the image I generated for you.";
        }

        saveMessage(textResponseForUser, Role.AGENT, conversation);
        return new ChatMessageResponse(textResponseForUser, conversation.getId(), imageDataForFrontend);
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
                - 'synthesizeText(goal, platform)' // NEW: 'platform' parameter added here
                - 'postToFacebook(content)'
                - 'postToTwitter(content)'

                If you need to generate text based on gathered information, use the special tool name 'synthesizeText'
                and describe the goal in a 'goal' parameter. The 'synthesizeText' tool now also requires a 'platform' parameter
                which should be either 'Facebook' or 'Twitter'. This is crucial for tailoring the content.

                The 'postToFacebook' and 'postToTwitter' tools require content that must be generated by the 'synthesizeText' step first.

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
        You are an expert, helpful, and collaborative social media marketing agent. Your goal is to assist the user by creating content,
        providing ideas, and performing actions based on their instructions.

        **--- YOUR WORKFLOW AND RULES ---**

        1.  **UNDERSTAND AND GATHER INFORMATION:**
            - First, analyze the user's request in the context of the entire conversation.
            - If you need information to fulfill the request (like news or post inspiration), use your information-gathering tools
            (`getMarketNews`, `getTopPerformingPosts`) silently as a first step.
            - **TOPIC RULE:** If `getMarketNews` needs a `topic` and the user is general (e.g., "latest news"),
             you MUST use the default topic `'business OR technology'`. DO NOT ask the user for a topic.

        2.  **CONTEXT AWARENESS - VERY IMPORTANT:**
             - **Image Posting Context:** If the user asks you to post an image, and an image was successfully generated
              in the immediately preceding turn (look for a message starting with `IMAGE_RESULT:::` in the conversation history), 
              you MUST assume the user wants to post *that specific image*.
               - **RULE FOR EXTRACTING URL:** You must extract **ONLY the URL part** (the string that starts with `http://...`) from 
               the `IMAGE_RESULT:::` message and pass that clean URL string to the `imageUrl` parameter of the `postImageToFacebook` tool.
                Do NOT include the "IMAGE_RESULT:::" prefix in the `imageUrl` parameter.
               - You should still ask for confirmation before posting, but your confirmation question should be about the *action*, 
               not about generating a new image. For example: "Got it. I will post the image we just created with the caption '...'. Is that correct?"

        3.  **DETERMINE FINAL ACTION:** Based on the user's request, decide your final action:

            - **IF the request is for an IMAGE or ANIMATED IMAGE:** This is a creative action. Execute it immediately without confirmation.
            Your process is to first create a detailed, descriptive prompt IN ENGLISH, and then call the `generateImage` for static pictures or `generateAnimatedImage` for moving pictures/GIFs.
            Your turn ends after you call the tool.

            - **IF the request is for an IRREVERSIBLE ACTION (e.g., `postToFacebook`, `postImageToFacebook`, `createTask`)
            :** You MUST first present your draft or plan to the user and ask for their explicit confirmation
            (e.g., "Here is the draft... Shall I publish it?"). Only call the actual tool in your NEXT turn, after the user has agreed.
            **To post an image, use the `postImageToFacebook` tool with the `imageUrl` and a `caption`.**

            - **IF the request is ONLY for information or ideas:** Provide the information directly and cleanly as your final answer.

        **--- OUTPUT FORMATTING ---**
        - When a tool returns a result prefixed with "IMAGE_RESULT:::", your final response to the user MUST be that
        exact result string and nothing else.

        ---
        CONVERSATION HISTORY:
        %s
        ---
        """.formatted(conversationHistory);
    }

    /**
     * Retrieves all chat conversations for a given user ID, ordered by creation
     * timestamp.
     *
     * @param userId The ID of the user for whom to retrieve conversations.
     * @return A list of ChatConversationResponse DTOs.
     */
    public List<ChatConversationResponse> getConversationsByUserId(Long userId) {
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
        return messageRepository.findByConversationIdOrderByCreationTimeStampAsc(conversationId);
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
        String cleaned = content.replaceAll("(?i)^.*?:\\s*", "").trim();
        return cleaned;
    }

    private String createSynthesisPrompt(String news, String topPosts, String goal, String platform) {
        String platformConstraint = "";
        if ("Twitter".equalsIgnoreCase(platform)) {
            platformConstraint = "The post must be concise and under 250 characters. Use hashtags relevant to the topic.";
        } else if ("Facebook".equalsIgnoreCase(platform)) {
            platformConstraint = "The post can be longer and more descriptive than a tweet. Aim for a friendly, conversational tone.";
        }


        return """
        Based on the following news: %s

        And this inspiration: %s

        Your task is to: %s

        **PLATFORM GUIDELINES:**
        - This post is for the platform: %s
        - %s

        IMPORTANT: Your entire response MUST BE ONLY the text for the social media post.
        Do not include any intro, preamble, or conversational text like "Here is the draft".
        DO NOT use any special formatting characters like asterisks or bolding.
        """.formatted(news, topPosts, goal, platform, platformConstraint);
    }

}
