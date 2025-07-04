package com.exjobb.backend.service;

import com.exjobb.backend.dto.ContentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    private final ChatClient geminiChatClient;

    public AgentService(@Qualifier("geminiChatClient") ChatClient chatClient) {
        this.geminiChatClient = chatClient;
    }

    /**
     * Main method which orchestrates the process from plan to finished content
     */
    public String generateFullContent(ContentRequest request){
        logger.info("Starting two-step-process for topic: {}", request.topic());

        // Step 1: Create a strategic plan
        String plan = generateContentPlan(request);
        logger.info("Step 1 finished. Plan generated:\n{}", plan);

        // Step 2: Use the plan to generate the final result
        String finalContent = createFinalContentFromPlan(request, plan);
        logger.info("Step 2 finished. Final content generated:\n{}", finalContent);

        return finalContent;
    }

    /**
     * Private help method for Step 1: Creating the plan
     * @param request
     * @return
     */
    private String generateContentPlan(ContentRequest request){
        logger.info("Generating content plan for topic: {}", request.topic());

        String userPrompt = """
             You are an expert on social media and content strategy.
             Your task is to create a detailed content plan based on the following information.
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
                .user(userPrompt)
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

}
