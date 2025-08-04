package com.exjobb.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public class UpdateTaskDTO {

    @NotBlank(message = "Prompt cannot be blank")
    private String prompt;

    // This field remains for advanced users or when the simple inputs aren't used.
    private String cronExpression;

    // --- NEW FIELDS for simple scheduling ---
    @Min(1)
    private Integer interval; // e.g., 5
    private String unit;      // e.g., "hours", "days", "minutes"


    // Getters and Setters
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public Integer getInterval() { return interval; }
    public void setInterval(Integer interval) { this.interval = interval; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}