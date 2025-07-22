package com.exjobb.backend.service;

import com.exjobb.backend.entity.SocialMediaPost;
import com.exjobb.backend.repository.SocialMediaPostRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternalDataToolService {

    private final SocialMediaPostRepository postRepository;

    public InternalDataToolService(SocialMediaPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Tool(name = "getTopPerformingPosts",
            description = "Gets a list of the 5 best-performing past posts based on engagement score. " +
                    "Use this to learn from successful content to match the style and tone..")
    public String getTopPerformingPosts(){

        List<SocialMediaPost> topPosts = postRepository.findFirst5ByOrderByEngagementScoreDesc();

        if(topPosts.isEmpty()){
            return "No past posts with engagement scores found to learn from.";
        }

        return "Here are some top-performing posts for inspiration:\n" +
                topPosts.stream()
                        .map(post -> "--- POST (Platform: " + post.getPlatform() + ") ---\n" + post.getContent())
                        .collect(Collectors.joining("\n\n"));

    }
}
