package com.exjobb.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ChatClientConfig {

    /**
     * Creates a specific ChatClient-bean for Vertex AI Gemini.
     * @Primary-annotation makes this the standard ChatClient if no specific
     * chatclient is chosen with @Qualifier annotation
     * We build the chat clients using builder so be able to add standard options, such as global tools (mcp).
     */
    @Bean
    @Primary
    public ChatClient geminiChatClient(VertexAiGeminiChatModel vertexAiGeminiChatModel,
                                       ToolCallbackProvider internalTools,
                                       ToolCallbackProvider externalTools) {

       List<ToolCallback> allToolCallbacks = new ArrayList<>();
       allToolCallbacks.addAll(Arrays.asList(internalTools.getToolCallbacks()));
       allToolCallbacks.addAll(Arrays.asList(externalTools.getToolCallbacks()));

       ToolCallingChatOptions defaultToolOptions = ToolCallingChatOptions.builder()
               .toolCallbacks(allToolCallbacks)
               .build();

        return ChatClient.builder(vertexAiGeminiChatModel)
                .defaultOptions(defaultToolOptions)
                .build();
    }


    // Can add additional models here
}
