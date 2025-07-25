package com.exjobb.backend.service;

import com.exjobb.backend.repository.SocialMediaPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExternalDataToolService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalDataToolService.class);
    private final SocialMediaPostRepository postRepository;

    public ExternalDataToolService(SocialMediaPostRepository postRepository) {
        this.postRepository = postRepository;
    }
}
