// src/main/java/com/exjobb/backend/service/ScheduledTaskService.java
package com.exjobb.backend.service;

import com.exjobb.backend.dto.ScheduledTaskDTO;
import com.exjobb.backend.dto.UpdateTaskDTO;
import com.exjobb.backend.entity.ScheduledTask;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.ScheduledTaskRepository;
import com.exjobb.backend.utils.CronExpressionTranslator; // <-- NEW IMPORT
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final CronExpressionTranslator cronExpressionTranslator; // <-- NEW FIELD

    public TaskService(ScheduledTaskRepository scheduledTaskRepository,
                       CronExpressionTranslator cronExpressionTranslator) { // <-- INJECT
        this.scheduledTaskRepository = scheduledTaskRepository;
        this.cronExpressionTranslator = cronExpressionTranslator; // <-- ASSIGN
    }

    @Transactional(readOnly = true)
    public List<ScheduledTaskDTO> getTasksForUser(Long userId) {
        List<ScheduledTask> tasks = scheduledTaskRepository.findByUserId(userId);
        return tasks.stream()
                .map(task -> {
                    String humanReadableCron = cronExpressionTranslator.translate(task.getCronExpression()); // <--
                    // TRANSLATE
                    // HERE
                    return ScheduledTaskDTO.fromEntity(task, humanReadableCron); // <-- PASS TRANSLATED STRING
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduledTaskDTO toggleTaskStatus(Long taskId, Long userId) {
        // 1. Hämta entiteten från databasen
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(
                        () -> new jakarta.persistence.EntityNotFoundException("Task not found with id: " + taskId));

        // 2. Behörighetskontroll: Säkerställ att den inloggade användaren äger
        // uppgiften
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
        // Notera: Detta antar att ni har en 'cronExpressionTranslator'-komponent
        // tillgänglig
        String humanReadableCron = cronExpressionTranslator.translate(updatedTask.getCronExpression());
        return ScheduledTaskDTO.fromEntity(updatedTask, humanReadableCron);
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User does not have permission to modify this task");
        }

        scheduledTaskRepository.delete(task);
        logger.info("Task {} deleted for user {}.", taskId, userId);
    }

    @Transactional
    public ScheduledTaskDTO updateTask(Long taskId, @Valid @RequestBody UpdateTaskDTO updateTaskDTO, Long userId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User does not have permission to modify this task");
        }

        // --- NEW: CRON GENERATION LOGIC ---
        String cronToSave = updateTaskDTO.getCronExpression();

        // If a cron expression was NOT provided, try to build one from the simple
        // inputs.
        if (cronToSave == null || cronToSave.isBlank()) {
            Integer interval = updateTaskDTO.getInterval();
            String unit = updateTaskDTO.getUnit();

            if (interval == null || unit == null) {
                throw new IllegalArgumentException(
                        "Either a cronExpression or both interval and unit must be provided.");
            }

            switch (unit.toLowerCase()) {
                case "minutes":
                    // Runs at the start of the second, every X minutes. e.g., "0 */5 * * * *"
                    cronToSave = String.format("0 */%d * * * *", interval);
                    break;
                case "hours":
                    // Runs at the start of the minute and second, every X hours. e.g., "0 0 */4 * *
                    // *"
                    cronToSave = String.format("0 0 */%d * * *", interval);
                    break;
                case "days":
                    // Runs at 9:00 AM every X days. e.g., "0 0 9 */2 * *"
                    cronToSave = String.format("0 0 9 */%d * *", interval);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid unit provided. Must be 'minutes', 'hours', or 'days'.");
            }
        }

        // Update task properties
        task.setPrompt(updateTaskDTO.getPrompt());
        task.setCronExpression(cronToSave);

        if (task.isActive()) {
            CronExpression cron = CronExpression.parse(task.getCronExpression());
            LocalDateTime nextRunTime = cron.next(LocalDateTime.now());
            task.setNextRunTime(nextRunTime);
            logger.info("Task {} has been updated. New next run time is {}", taskId, nextRunTime);
        }

        ScheduledTask updatedTask = scheduledTaskRepository.save(task);
        String humanReadableCron = cronExpressionTranslator.translate(updatedTask.getCronExpression());
        return ScheduledTaskDTO.fromEntity(updatedTask, humanReadableCron);
    }
}