package com.exjobb.backend.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GeminiTestRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(GeminiTestRunner.class);
    private final ChatClient chatClient;

    // Spring injicerar automatiskt en ChatClient.Builder som vi kan bygga vår klient med
    public GeminiTestRunner(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) throws Exception{
        logger.info("--- STARTAR GEMINI TEST ---");

        try{
            String response = chatClient.prompt()
                    .user("Är du redo? Svara bara med fyra ord.")
                    .call()
                    .content();

            logger.info("SVAR FRÅN GEMINI: {}", response);
            logger.info("--- TEST LYCKADES! ---");

        }catch(Exception e){
            logger.error("--- TEST MISSLYCKADES ---", e);
        }
    }
}
