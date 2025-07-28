package com.exjobb.backend.config;

import com.exjobb.backend.service.ExternalDataToolService;
import com.exjobb.backend.service.InternalDataToolService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    /**
     * This bean registers all the @Tool-annotated methods from
     * InternalDataToolService so that they are globally available for ChatClients
     */
    @Bean
    public ToolCallbackProvider internalTools(InternalDataToolService internalDataToolService) {
        return MethodToolCallbackProvider.builder().toolObjects(internalDataToolService).build();
    }

    /**
     * This bean registers all the @Tool-annotated methods from
     * ExternalDataToolService so that they are globally available for ChatClients
     */
    @Bean
    public ToolCallbackProvider externalTools(ExternalDataToolService externalDataToolService) {
        return MethodToolCallbackProvider.builder().toolObjects(externalDataToolService).build();
    }



}
