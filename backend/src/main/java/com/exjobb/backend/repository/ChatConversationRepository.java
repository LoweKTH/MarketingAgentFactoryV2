package com.exjobb.backend.repository;

import com.exjobb.backend.entity.ChatConversation;
import com.exjobb.backend.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    List<ChatConversation> findByUserOrderByCreationTimeStampDesc(User user);
    List<ChatConversation> findByUserIdOrderByCreationTimeStampDesc(Long userId);
}
