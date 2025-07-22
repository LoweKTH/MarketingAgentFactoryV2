package com.exjobb.backend.dto;

public record ChatMessageRequest(String message, Long conversationId) {
}
