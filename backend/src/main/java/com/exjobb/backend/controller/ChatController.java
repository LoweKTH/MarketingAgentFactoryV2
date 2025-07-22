package com.exjobb.backend.controller;

import com.exjobb.backend.dto.ChatMessageRequest;
import com.exjobb.backend.dto.ChatMessageResponse;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.service.AgentService;
import com.exjobb.backend.service.UserService;
import org.springframework.security.core.Authentication;
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
}
