// src/main/java/com/exjobb/backend/dto/ChatMessageDTO.java
package com.exjobb.backend.dto;

import java.time.LocalDateTime;

public record ChatMessageDTO(
        Long id,
        String message,
        String role,
        LocalDateTime creationTimestamp
) {}