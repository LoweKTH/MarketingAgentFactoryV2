// src/main/java/com/exjobb/backend/dto/ScheduledTaskDTO.java
package com.exjobb.backend.dto;

import com.exjobb.backend.entity.ScheduledTask;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ScheduledTaskDTO {

    private Long id;
    private String prompt;
    private String cronExpression;
    private String humanReadableCronExpression; // <-- NEW FIELD
    private LocalDateTime nextRunTime;
    private LocalDateTime lastRunTime;
    private boolean active; // Changed from isActive to active for consistency with JSON

    // Constructor with new field
    public ScheduledTaskDTO(Long id, String prompt, String cronExpression, String humanReadableCronExpression, LocalDateTime nextRunTime, LocalDateTime lastRunTime, boolean active) {
        this.id = id;
        this.prompt = prompt;
        this.cronExpression = cronExpression;
        this.humanReadableCronExpression = humanReadableCronExpression; // Assign new field
        this.nextRunTime = nextRunTime;
        this.lastRunTime = lastRunTime;
        this.active = active;
    }

    public static ScheduledTaskDTO fromEntity(ScheduledTask task, String humanReadableCron) { // Modified to accept translated cron
        return new ScheduledTaskDTO(
                task.getId(),
                task.getPrompt(),
                task.getCronExpression(),
                humanReadableCron, // Pass the translated string
                task.getNextRunTime(),
                task.getLastRunTime(),
                task.isActive()
        );
    }
    // You might want to update your `isActive()` getter to `isActive()` for Lombok's @Data
    // and your DTO to `private boolean active;` for consistent naming with JSON.
    // I've updated it to `active` based on your previous JSON.
}