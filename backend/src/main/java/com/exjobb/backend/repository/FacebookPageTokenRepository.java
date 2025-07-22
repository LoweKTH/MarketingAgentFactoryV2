package com.exjobb.backend.repository;

import com.exjobb.backend.entity.FacebookPageToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.exjobb.backend.entity.User;
import java.util.List;

public interface FacebookPageTokenRepository extends JpaRepository<FacebookPageToken, Long> {
    Optional<FacebookPageToken> findByPageId(String pageId);
    List<FacebookPageToken> findAllByUser(User user);
}
