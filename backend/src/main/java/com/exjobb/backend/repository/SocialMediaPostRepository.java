package com.exjobb.backend.repository;

import com.exjobb.backend.entity.SocialMediaPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMediaPostRepository extends JpaRepository<SocialMediaPost, Long> {
    SocialMediaPost findByContent(String content);
}
