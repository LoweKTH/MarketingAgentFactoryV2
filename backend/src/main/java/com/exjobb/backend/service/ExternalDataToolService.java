package com.exjobb.backend.service;

import com.exjobb.backend.entity.ScheduledTask;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.ScheduledTaskRepository;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import com.exjobb.backend.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExternalDataToolService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalDataToolService.class);
    private final SocialMediaPostRepository postRepository;
    private final String newsApiKey;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final UserRepository userRepository;

    public ExternalDataToolService(SocialMediaPostRepository postRepository,
                                   @Value("${news.api.key}") String newsApiKey,
                                   ScheduledTaskRepository scheduledTaskRepository,
                                   UserRepository userRepository){
        this.postRepository = postRepository;
        this.newsApiKey = newsApiKey;
        this.scheduledTaskRepository = scheduledTaskRepository;
        this.userRepository = userRepository;
        logger.info("News API key loaded: {}", newsApiKey);
    }

    // Records for JSON-parsing
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NewsArticle(String title, String description){}
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NewsDataResponse(List<NewsArticle> results){}

    @Tool(description = "Gets recent news headlines for a given topic and country. " +
            "Use this to find trending information and create timely content.")
    public String getMarketNews(String topic, String countryCode){
        logger.info("--- TOOL CALLED: getKenyanMarketNews ---");

        final String url = "https://newsdata.io/api/1/news?apikey=" + this.newsApiKey +
                "&country=" + countryCode +
                "&language=en" +
                "&excludedomain=za.ign.com" +
                "&q=" + topic;

        RestTemplate restTemplate = new RestTemplate();
        try {
            NewsDataResponse response = restTemplate.getForObject(url, NewsDataResponse.class);

            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response.results().stream()
                        .limit(10)
                        .map(article -> "- " + article.title)
                        .collect(Collectors.joining("\n"));
            }
            return "No relevant news found for topic '" + topic + "' in country '" + countryCode + "'.";
        } catch (Exception e) {
            logger.error("Error calling Newsdata.io API: {}", e.getMessage());
            return "Could not fetch news at this time.";
        }
    }

    @Tool(description = "Schedules a new recurring task for the agent to perform. " +
            "Use this when a user asks for an action to be repeated on a schedule (e.g., 'every day', 'every 12 hours')")
    public String createTask(String prompt, String cronExpression){

        // 1st, get the current authentication information from spring security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("Authentication object not found or user is not authenticated.");
            return "Error: You must be authenticated to schedule a task.";
        }
        
        String username = authentication.getName();
        
        // 2nd, get full user-entity from db
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException
                        ("Authenticated user '" + username + "' not found in database."));


        logger.info("--- TOOL CALLED: createTask for user '{}' with schedule '{}' ---",
                currentUser.getUsername(), cronExpression);

        String correctedCron = cronExpression;
        String[] cronParts = cronExpression.trim().split("\\s+");
        if(cronParts.length == 5){
            // If we got an expression with 5 cronParts, add a 0 for seconds in the beginning
            correctedCron = "0 " + cronExpression;
            logger.warn("Corrected 5-field cron from LLM to 6-field cron: '{}'", correctedCron);
        }

        ScheduledTask newTask = new ScheduledTask(prompt, correctedCron, currentUser);

        try {
            CronExpression cron = CronExpression.parse(newTask.getCronExpression());
            LocalDateTime firstRunTime = cron.next(LocalDateTime.now());
            newTask.setNextRunTime(firstRunTime);

            scheduledTaskRepository.save(newTask);

            return "Task successfully scheduled for user " + currentUser.getUsername() + ". The next run will be at: " + firstRunTime;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid cron expression provided: {}", cronExpression, e);
            return "Error: The provided schedule is invalid. Please use a valid cron expression.";
        }
    }

}
