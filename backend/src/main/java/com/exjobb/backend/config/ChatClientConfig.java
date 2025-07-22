package com.exjobb.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

    /**
     * Creates a specific ChatClient-bean for Vertex AI Gemini.
     * @Primary-annotation makes this the standard ChatClient if no specific
     * chatclient is chosen with @Qualifier annotation
     */
    @Bean
    @Primary
    public ChatClient geminiChatClient(VertexAiGeminiChatModel vertexAiGeminiChatModel) {
        return ChatClient.create(vertexAiGeminiChatModel);
    }


    // Can add additional models here
}
