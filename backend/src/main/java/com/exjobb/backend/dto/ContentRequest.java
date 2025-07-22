package com.exjobb.backend.dto;

public record ContentRequest(String topic,
                             String targetPlatform,
                             String extraInstructions) {}

