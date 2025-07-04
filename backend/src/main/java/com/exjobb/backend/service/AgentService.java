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


    public String generateContentPlan(ContentRequest request){
        logger.info("Genererar innehållsplan för ämne: {}", request.topic());

        // Bygg en avancerad prompt med persona, uppgift, indata och önskat output-format.
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

}
