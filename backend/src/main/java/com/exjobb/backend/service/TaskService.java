// src/main/java/com/exjobb/backend/service/ScheduledTaskService.java
package com.exjobb.backend.service;

import com.exjobb.backend.dto.ScheduledTaskDTO;
import com.exjobb.backend.entity.ScheduledTask;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.ScheduledTaskRepository;
import com.exjobb.backend.utils.CronExpressionTranslator; // <-- NEW IMPORT
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final CronExpressionTranslator cronExpressionTranslator; // <-- NEW FIELD

    public TaskService(ScheduledTaskRepository scheduledTaskRepository, CronExpressionTranslator cronExpressionTranslator) { // <-- INJECT
        this.scheduledTaskRepository = scheduledTaskRepository;
        this.cronExpressionTranslator = cronExpressionTranslator; // <-- ASSIGN
    }

    @Transactional(readOnly = true)
    public List<ScheduledTaskDTO> getTasksForUser(Long userId) {
        List<ScheduledTask> tasks = scheduledTaskRepository.findByUserId(userId);
        return tasks.stream()
                .map(task -> {
                    String humanReadableCron = cronExpressionTranslator.translate(task.getCronExpression()); // <-- TRANSLATE HERE
                    return ScheduledTaskDTO.fromEntity(task, humanReadableCron); // <-- PASS TRANSLATED STRING
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduledTaskDTO toggleTaskStatus(Long taskId, Long userId) {
        // 1. Hämta entiteten från databasen
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Task not found with id: " + taskId));

        // 2. Behörighetskontroll: Säkerställ att den inloggade användaren äger uppgiften
        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User does not have permission to modify this task");
        }

        boolean wasActive = task.isActive();

        // 3. Växla den aktiva statusen
        task.setActive(!wasActive);

        // 4. Om uppgiften precis blev aktiverad, beräkna en ny körtid
        if (!wasActive) { // Villkoret är sant om den VAR inaktiv och nu blir aktiv
            logger.info("Task {} has been reactivated. Recalculating next run time.", taskId);

            CronExpression cron = CronExpression.parse(task.getCronExpression());
            LocalDateTime nextRunTime = cron.next(LocalDateTime.now());
            task.setNextRunTime(nextRunTime);

            logger.info("New next run time for task {} is set to {}", taskId, nextRunTime);
        } else {
            logger.info("Task {} has been paused.", taskId);
        }

        // 5. Spara den uppdaterade entiteten
        ScheduledTask updatedTask = scheduledTaskRepository.save(task);

        // 6. Översätt den sparade entiteten till en DTO och returnera den
        // Notera: Detta antar att ni har en 'cronExpressionTranslator'-komponent tillgänglig
        String humanReadableCron = cronExpressionTranslator.translate(updatedTask.getCronExpression());
        return ScheduledTaskDTO.fromEntity(updatedTask, humanReadableCron);
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId){
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        if(!task.getUser().getId().equals(userId)){
            throw new AccessDeniedException("User does not have permission to modify this task");
        }

        scheduledTaskRepository.delete(task);
        logger.info("Task {} deleted for user {}.", taskId, userId);
    }
}