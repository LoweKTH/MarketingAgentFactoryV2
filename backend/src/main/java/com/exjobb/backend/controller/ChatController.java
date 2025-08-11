package com.exjobb.backend.controller;

import com.exjobb.backend.dto.ChatConversationResponse;
import com.exjobb.backend.dto.ChatMessageDTO;
import com.exjobb.backend.dto.ChatMessageRequest;
import com.exjobb.backend.dto.ChatMessageResponse;
import com.exjobb.backend.entity.ChatMessage;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.service.agent.AgentService;
import com.exjobb.backend.service.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AgentService agentService;
    private final UserService userService;

    public ChatController(AgentService agentService, UserService userService) {
        this.agentService = agentService;
        this.userService = userService;
    }

    @PostMapping
    public ChatMessageResponse handleChatMessage(@RequestBody ChatMessageRequest request,
                                                 Authentication authentication){
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        return agentService.handleChatMessage(request, currentUser);
    }

    @GetMapping("/conversations")
    public List<ChatConversationResponse> getUserConversations(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        System.out.println(currentUser.getId());
        return agentService.getConversationsByUserId(currentUser.getId());
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageDTO> getConversationMessages(@PathVariable Long conversationId) {

        List<ChatMessage> messages = agentService.getMessagesByConversationId(conversationId);


        return messages.stream()
                .map(msg -> new ChatMessageDTO(
                        msg.getId(),
                        msg.getMessage(),
                        msg.getRole().name(),
                        msg.getCreationTimeStamp()
                ))
                .collect(Collectors.toList());
    }
}
