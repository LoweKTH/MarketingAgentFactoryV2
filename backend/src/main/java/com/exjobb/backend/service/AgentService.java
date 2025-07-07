package com.exjobb.backend.service;

import com.exjobb.backend.dto.ContentRequest;
import com.exjobb.backend.entity.SocialMediaPost;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    private static final int MAX_ATTEMPTS = 3;
    private final ChatClient geminiChatClient;
    private final SocialMediaPostRepository postRepository;


    public AgentService(@Qualifier("geminiChatClient") ChatClient chatClient,
                        SocialMediaPostRepository postRepository) {
        this.geminiChatClient = chatClient;
        this.postRepository = postRepository;
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

}
