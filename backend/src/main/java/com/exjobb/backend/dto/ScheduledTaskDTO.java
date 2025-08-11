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
    private String humanReadableCronExpression;
    private LocalDateTime nextRunTime;
    private LocalDateTime lastRunTime;
    private boolean active;


    public ScheduledTaskDTO(Long id, String prompt, String cronExpression, String humanReadableCronExpression, LocalDateTime nextRunTime, LocalDateTime lastRunTime, boolean active) {
        this.id = id;
        this.prompt = prompt;
        this.cronExpression = cronExpression;
        this.humanReadableCronExpression = humanReadableCronExpression;
        this.nextRunTime = nextRunTime;
        this.lastRunTime = lastRunTime;
        this.active = active;
    }

    public static ScheduledTaskDTO fromEntity(ScheduledTask task, String humanReadableCron) {
        return new ScheduledTaskDTO(
                task.getId(),
                task.getPrompt(),
                task.getCronExpression(),
                humanReadableCron,
                task.getNextRunTime(),
                task.getLastRunTime(),
                task.isActive()
        );
    }

}