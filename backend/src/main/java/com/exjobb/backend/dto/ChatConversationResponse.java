package com.exjobb.backend.dto;

import java.time.LocalDateTime;

public record ChatConversationResponse(
        Long id,
        String title,
        LocalDateTime creationTimeStamp
) {}