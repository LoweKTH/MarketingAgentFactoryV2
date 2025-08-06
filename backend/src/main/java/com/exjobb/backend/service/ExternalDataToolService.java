package com.exjobb.backend.service;

import com.exjobb.backend.entity.ScheduledTask;
import com.exjobb.backend.entity.SocialMediaPost;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.ScheduledTaskRepository;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import com.exjobb.backend.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.cloud.aiplatform.v1.*;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;

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
    private final FacebookService facebookService;
    private final TwitterService twitterService;
    private final SocialMediaPostRepository socialMediaPostRepository;
    private final ImageGenerationService vertexImageGenerator;

    public ExternalDataToolService(SocialMediaPostRepository postRepository,
            @Value("${news.api.key}") String newsApiKey,
            ScheduledTaskRepository scheduledTaskRepository,
            UserRepository userRepository,
            FacebookService facebookService,
            TwitterService twitterService,
            SocialMediaPostRepository socialMediaPostRepository,
            @Qualifier("vertexImageGenerator") ImageGenerationService vertexImageGenerator) {
        this.postRepository = postRepository;
        this.newsApiKey = newsApiKey;
        this.scheduledTaskRepository = scheduledTaskRepository;
        this.userRepository = userRepository;
        this.facebookService = facebookService;
        this.twitterService = twitterService;
        this.socialMediaPostRepository = socialMediaPostRepository;
        this.vertexImageGenerator = vertexImageGenerator;
        logger.info("News API key loaded: {}", newsApiKey);
    }

    // Records for JSON-parsing
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NewsArticle(String title, String description) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NewsDataResponse(List<NewsArticle> results) {
    }

    @Tool(description = "Gets recent news headlines for a given topic and country. " +
            "Use this to find trending information and create timely content.")
    public String getMarketNews(String topic, String countryCode) {
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

    @Tool(description = "Schedules a new recurring task for the agent to perform...")
    public String createTask(String prompt, String cronExpression) {
        try {
            User currentUser = getCurrentAuthenticatedUser();

            logger.info("--- TOOL CALLED: createTask ---");

            String correctedCron = cronExpression;
            String[] cronParts = cronExpression.trim().split("\\s+");
            if (cronParts.length == 5) {
                correctedCron = "0 " + cronExpression;
                logger.warn("Corrected 5-field cron from LLM to 6-field cron: {}", correctedCron);
            }

            ScheduledTask newTask = new ScheduledTask(prompt, correctedCron, currentUser);

            CronExpression cron = CronExpression.parse(newTask.getCronExpression());
            LocalDateTime firstRunTime = cron.next(LocalDateTime.now());
            newTask.setNextRunTime(firstRunTime);

            scheduledTaskRepository.save(newTask);

            return "Task successfully scheduled for user " + currentUser.getUsername()
                    + ". The next run will be at: " + firstRunTime;
        } catch (Exception e) {
            logger.error("Error scheduling task: {}", e.getMessage());
            return "Error scheduling task: " + e.getMessage();
        }
    }

    @Tool(description = "Call this tool as the FINAL step to publish a completed text post to Facebook...")
    // Add the User parameter to the method signature
    public String postToFacebook(String content) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            logger.info("--- TOOL CALLED: postToFacebook for user {} ---", currentUser.getUsername());

            // Use the passed-in user object
            saveSocialMediaPost(content, "Facebook", currentUser);
            facebookService.postToUserFirstPage(currentUser.getUsername(), content);

            return "The post was successfully saved internally and published to Facebook.";
        } catch (Exception e) {
            logger.error("Error posting to Facebook: {}", e.getMessage());
            return "Error posting to Facebook: " + e.getMessage();
        }
    }

    /**
     * This is the internal method for backend use. It is NOT annotated with @Tool.
     * It requires the User object to be provided explicitly.
     * This will be used by your automatic/scheduled task runner.
     */
    public String postToFacebook(String content, User user) {
        try {
            logger.info("--- EXECUTING postToFacebook for user {} ---", user.getUsername());

            // Use the passed-in user object for all operations
            saveSocialMediaPost(content, "Facebook", user);
            facebookService.postToUserFirstPage(user.getUsername(), content);

            return "The post was successfully saved internally and published to Facebook.";
        } catch (Exception e) {
            logger.error("Error posting to Facebook: {}", e.getMessage());
            return "Error posting to Facebook: " + e.getMessage();
        }
    }

    @Tool(description = "Call this tool as the FINAL step to publish a completed text post to Twitter.")
    public String postToTwitter(String content) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            logger.info("--- TOOL CALLED: postToTwitter for user {} ---", currentUser.getUsername());

            // Save the post to your internal database
            saveSocialMediaPost(content, "Twitter", currentUser);

            // Use the TwitterService to post the tweet
            twitterService.postTweetForUser(currentUser, content);

            return "The post was successfully saved internally and published to Twitter.";
        } catch (Exception e) {
            logger.error("Error posting to Twitter: {}", e.getMessage());
            return "Error posting to Twitter: " + e.getMessage();
        }
    }

    /**
     * This is the internal method for backend use (e.g., scheduled tasks).
     * It is NOT annotated with @Tool.
     * It requires the User object to be provided explicitly.
     */
    public String postToTwitter(String content, User user) {
        try {
            logger.info("--- EXECUTING postToTwitter for user {} ---", user.getUsername());

            // Save the post to your internal database
            saveSocialMediaPost(content, "Twitter", user);

            // Use the TwitterService to post the tweet
            twitterService.postTweetForUser(user, content);

            return "The post was successfully saved internally and published to Twitter.";
        } catch (Exception e) {
            logger.error("Error posting to Twitter: {}", e.getMessage());
            return "Error posting to Twitter: " + e.getMessage();
        }
    }

    @Tool(description = "Generates an image based on a detailed text prompt. " +
            "The user can optionally specify a provider, for example 'google'. " +
            "If no provider is specified, 'google' will be used as the default.")
    public String generateImage(String imagePrompt, String provider){
        logger.info("--- TOOL CALLED: generateImage with provider: {} ---", provider);

        String providerToUse = (provider != null && !provider.isBlank()) ? provider.toLowerCase() : "google";

        try{
            switch(providerToUse){
                case "google":
                    return vertexImageGenerator.generateImage(imagePrompt);
                //case "dalle":
                    //return dalleImageGenerator.generateImage(imagePrompt);
                default:
                    return "Error: Unknown image generation provider '" + providerToUse + "'.";
            }
        }catch(Exception e){
            return e.getMessage();
        }
    }


    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated or authentication context is missing.");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new RuntimeException("Authenticated user '" + username + "' not found in database."));
    }

    private SocialMediaPost saveSocialMediaPost(String content, String platform, User currentUser) {
        logger.info("Saving generated post for platform '{}' to the database for user '{}'.", platform,
                currentUser.getUsername());
        SocialMediaPost newPost = new SocialMediaPost();
        newPost.setContent(content);
        newPost.setPlatform(platform);
        newPost.setUser(currentUser);
        newPost.setEngagementScore(0.0);
        newPost.setIsApprovedByUser(true);
        return socialMediaPostRepository.save(newPost);
    }

}
