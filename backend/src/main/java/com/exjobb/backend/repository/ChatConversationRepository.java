package com.exjobb.backend.repository;

import com.exjobb.backend.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

}
