package com.exjobb.backend.service;

import com.exjobb.backend.entity.SocialMediaPost;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternalDataToolService {

    private static final Logger logger = LoggerFactory.getLogger(InternalDataToolService.class);
    private final SocialMediaPostRepository postRepository;

    public InternalDataToolService(SocialMediaPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Tool(name = "getTopPerformingPosts",
            description = "Gets a list of the 5 best-performing past posts based on engagement score. " +
                    "Use this to learn from successful content to match the style and tone..")
    public String getTopPerformingPosts(){

        logger.info("--- TOOL CALLED: getTopPerformingPosts ---");

        List<SocialMediaPost> topPosts = postRepository.findFirst5ByOrderByEngagementScoreDesc();

        if(topPosts.isEmpty()){
            logger.warn("No past posts with engagement scores found to learn from.");
            return "No past posts with engagement scores found to learn from.";
        }
        logger.info("Top posts found: {}", topPosts.size());
        topPosts.forEach(post ->
                logger.info("  - ID: {}, Score: {}, Content: {}...",
                        post.getId(),
                        post.getEngagementScore(),
                        post.getContent().substring(0, Math.min(post.getContent().length(), 50)))
        );
        logger.info("-------------------------------------------");

        return "Here are some top-performing posts for inspiration:\n" +
                topPosts.stream()
                        .map(post -> "--- POST (Platform: " + post.getPlatform() + ") ---\n" + post.getContent())
                        .collect(Collectors.joining("\n\n"));

    }


}
