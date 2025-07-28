// src/main/java/com/exjobb/backend/dto/ChatMessageDTO.java
package com.exjobb.backend.dto;

import java.time.LocalDateTime;

public record ChatMessageDTO(
        Long id,
        String message, // The content of the chat message
        String role,    // The role of the sender (e.g., "USER", "AGENT")
        LocalDateTime creationTimestamp
) {}