package com.exjobb.backend.service;

import com.exjobb.backend.dto.ChatMessageRequest;
import com.exjobb.backend.dto.ChatMessageResponse;
import com.exjobb.backend.dto.ContentRequest;
import com.exjobb.backend.dto.ExtractedParams;
import com.exjobb.backend.entity.*;
import com.exjobb.backend.repository.ChatConversationRepository;
import com.exjobb.backend.repository.ChatMessageRepository;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import io.grpc.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    private static final int MAX_ATTEMPTS = 5;
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

    public ChatMessageResponse handleChatMessage(ChatMessageRequest request, User currentUser){
        ChatConversation conversation;
        List<ChatMessage> history;

        // Find or create conversation
        if(request.conversationId() != null){
            conversation = conversationRepository.findById(request.conversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + request.conversationId()));
            // Load history for this conversation
            history = messageRepository.findByConversationIdOrderByCreationTimeStampAsc(conversation.getId());
        }else{
            conversation = new ChatConversation();
            conversation.setUser(currentUser);
            conversation.setTitle(request.message().substring(0, Math.min(request.message().length(), 50)));
            conversationRepository.save(conversation);
            history = List.of();
            logger.info("New conversation created with title: {}", conversation.getTitle());
        }

        // Save users message
        ChatMessage userChatMessage = new ChatMessage();
        userChatMessage.setConversation(conversation);
        userChatMessage.setMessage(request.message());
        userChatMessage.setRole(Role.USER);
        messageRepository.save(userChatMessage);

        // Build a string of the history for the promp
        String historyString = history.stream()
                .map(msg -> msg.getRole().name() + ": " + msg.getMessage())
                .collect(Collectors.joining("\n"));

        // Use the parameter extraction based on the entire conversation and create a variable which depending on what
        // was extracted will either display a message such as a question, or a direct answer to the query
        ExtractedParams params = extractParametersFromMessage(historyString + "\nUSER: " + request.message());
        String agentResponse;

        if(params.topic() == null || params.topic().isBlank()){
            agentResponse = "Of course! What topic should the post be about?";
        }else if(params.platform() == null || params.platform().isBlank()){
            agentResponse = "Great! For which platform would you like to post? (e.g., LinkedIn, Twitter, Instagram)";
        }else{
            // All information exists, apply defaults and run main process
            String tone = params.tone();
            if(tone == null || tone.isBlank()){
               if(params.platform().equalsIgnoreCase("LinkedIn")){
                   tone = "professional and insightful";
               } else {
                   tone = "engaging and friendly";
               }
               logger.info("No tone specified, applying default for {}: {}", params.platform(), tone);
            }
            ContentRequest completeRequest = new ContentRequest(params.topic(),
                    params.platform(),
                    "Tone of voice should be: " + tone);
            SocialMediaPost finalPost = generateFullContent(completeRequest, currentUser);

            if(finalPost != null){
                agentResponse = finalPost.getContent();
            }else{
                agentResponse = "I'm sorry, I failed to create content that passed the quality check." +
                        " Please try rephrasing your request.";
            }
        }

        // Save agents response
        ChatMessage agentChatMessage = new ChatMessage();
        agentChatMessage.setConversation(conversation);
        agentChatMessage.setMessage(agentResponse);
        agentChatMessage.setRole(Role.AGENT);
        messageRepository.save(agentChatMessage);
        logger.info("Agents response saved to conversation {}", conversation.getId());

        return new ChatMessageResponse(agentResponse, conversation.getId());
    }

    /**
     * Main method which orchestrates the process from plan to finished content with feedback-loop
     */
    public SocialMediaPost generateFullContent(ContentRequest request, User user){
        logger.info("Starting process with feedback-loop for topic: {}", request.topic());

        // Step 1: Create a strategic plan
        String plan = generateContentPlan(request);
        logger.info("Step 1 finished. Plan generated:\n{}", plan);

        SocialMediaPost finalPost = null;
        for(int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++){
            logger.info("Attempt {} of {}", attempt, MAX_ATTEMPTS);

            // Step 2: Use the plan to generate the final content
            String currentDraft = createFinalContentFromPlan(request, plan);
            logger.info("Step 2 finished. Draft generated.");

            // Step 3: Quality control of draft
            if(validateContent(request, plan, currentDraft)){
                logger.info("Draft is good enough.  Returning final content and saving to database.");
                SocialMediaPost postToSave = new SocialMediaPost();
                postToSave.setContent(currentDraft);
                postToSave.setPlatform(request.targetPlatform());
                postToSave.setUser(user);
                // Can add future attributes here in the future, such as qualityScore for example

                finalPost = postRepository.save(postToSave);
                logger.info("Post saved with ID: {}", finalPost.getId());

                break;
            }else{
                logger.info("Draft is not good enough. Trying again...");
                if(attempt == MAX_ATTEMPTS){
                    logger.error("Max attempts reached. Failed to create an approved post.");
                    // Here we could throw an exception or handle the error differently. Fix later.
                }
            }
        }

        return finalPost; // Return the saved object (could be null if loop fails).
    }

    /**
     * Private help method for Step 1: Creating the plan
     * @return
     */
    private String generateContentPlan(ContentRequest request){
        logger.info("Generating content plan for topic: {}", request.topic());

        String planPrompt = """
             You are an expert on social media and content strategy.
             Your task is to create a detailed content plan based on the following information.
             
                You have access to the following tools. Follow these guidelines for when to use them:
                - Tool Usage Guideline: You MUST use a tool if the user's request explicitly asks for it (e.g., "use past posts as inspiration").
                 You SHOULD use a tool if its description is highly relevant to making the plan more specific and tailored to the user's needs. 
                 For example, using "getTopPerformingPosts" is almost always a good idea to better match the company's voice.
                - Available Tools:
                  - getTopPerformingPosts(): Gets a list of the 5 best-performing past posts based on engagement score. 
                  Use this to learn from successful content to match the style and tone.
             Respond in English.

             ---
             INPUT:
             - Topic: %s
             - Target Platform: %s
             - Extra instructions: %s
             ---

             OUTPUT-FORMAT:
             Generate a plan with the following headings and structure:

             **Target Audience Analysis:**
             - Describe the primary target audience for this post on the specified platform.

             **Core Message:**
             - Formulate the single most important message the post should convey in one sentence.

             **Tone of Voice:**
             - Describe the appropriate tone of voice (e.g., professional, inspirational, humorous).

             **Proposed Structure:**
             - **Hook:** A suggestion for a powerful first sentence to capture the reader's interest.
             - **Content Points:** 3-5 bullet points summarizing what the post should cover.
             - **Call-to-Action (CTA):** A suggestion for a clear call-to-action at the end of the post.
                """.formatted(request.topic(), request.targetPlatform(), request.extraInstructions());

        return geminiChatClient.prompt()
                .user(planPrompt)
                .tools(this.internalDataToolService)
                .call()
                .content();
    }

    /**
     * Private help method for step 2: Creating the final content based on the plan
     */
    private String createFinalContentFromPlan(ContentRequest request, String plan){

        String contentPrompt = """
                You are an expert copywriter for social media.
                Your task is to write a compelling social media post.
                You must strictly follow the provided strategic plan.
                
                ---
                ORIGINAL REQUEST:
                - Topic: %s
                - Target Platform: %s
                - Extra instructions: %s
                ---
                STRATEGIC PLAN TO FOLLOW:
                %s
                ---
                
                Write the final, ready-to-publish social media post now.
                """.formatted(request.topic(), request.targetPlatform(),
                request.extraInstructions(), plan);

        return geminiChatClient.prompt()
                .user(contentPrompt)
                .call()
                .content();
    }

    /**
     * Private help method for step 3: acts as a critic who evaluates the content
     * @return true if contenet is good enough, otherwise returns false
     */
    private boolean validateContent(ContentRequest request, String plan, String generatedContent){
        logger.info("Performing quality control...");

        String criticPrompt = """
                You are a strict and meticulous content editor. Your only task is to evaluate a generateds social media post.
                You must check if the generated content strictly follows the provided strategic plan.
                The content MUST fulfill ALL criteria in the plan.
                
                ---
                ORIGINAL REQUEST:
                - Topic: %s
                - Target Platform: %s
                ---
                STRATEGIC PLAN THAT MUST BE FOLLOWED:
                %s
                ---
                GENERATED CONTENT TO EVALUATE:
                %s
                ---
                
                QUESTION: Does the 'GENERATED CONTENT' perfectly match all requirements in the 'STRATEGIC PLAN'?
                Your answer MUST be a single word: YES or NO.
                """.formatted(request.topic(), request.targetPlatform(), plan, generatedContent);

        String evaluation = geminiChatClient.prompt()
                .user(criticPrompt)
                .call()
                .content();
        logger.info("Critics answer: {}", evaluation);
        return evaluation.trim().equalsIgnoreCase("YES");

    }

    /**
     * Private help method which is supposed to make out the parameters of the users message, and handle if there are none
     * @param userMessage
     * @return
     */
    private ExtractedParams extractParametersFromMessage(String userMessage){
        logger.info("Extracting parameters from message: {}", userMessage);

        String extractionPrompt = """
                Analyze the user's request and extract the key parameters for a social media post.
                Your response MUST be a valid JSON object. Do not add any text before or after the JSON.
                The JSON object should have three fields: "topic", "platform" and "tone".
                If a parameter is not mentioned in the user's request, its value in the JSON should be null.  
                
                Example:
                User request: "write a funny post for instagram about cats"
                Your JSON response:
                {
                    "topic": "cats",
                    "platform": "instagram",
                    "tone": "funny"
                }
                ---
                USER REQUEST TO ANALYZE:
                %s
                ---
                YOUR JSON RESPONSE:
                """.formatted(userMessage);
        
        // .entity() tries to automatically transform the JSON-response to our ExtractedParams record
        return geminiChatClient.prompt()
                .user(extractionPrompt)
                .call()
                .entity(ExtractedParams.class);
    }

}
