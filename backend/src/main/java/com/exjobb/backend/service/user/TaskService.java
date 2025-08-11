
package com.exjobb.backend.service.user;

import com.exjobb.backend.dto.ScheduledTaskDTO;
import com.exjobb.backend.dto.UpdateTaskDTO;
import com.exjobb.backend.entity.ScheduledTask;
import com.exjobb.backend.repository.ScheduledTaskRepository;
import com.exjobb.backend.utils.CronExpressionTranslator;
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
    private final CronExpressionTranslator cronExpressionTranslator;

    public TaskService(ScheduledTaskRepository scheduledTaskRepository,
                       CronExpressionTranslator cronExpressionTranslator) {
        this.scheduledTaskRepository = scheduledTaskRepository;
        this.cronExpressionTranslator = cronExpressionTranslator;
    }

    @Transactional(readOnly = true)
    public List<ScheduledTaskDTO> getTasksForUser(Long userId) {
        List<ScheduledTask> tasks = scheduledTaskRepository.findByUserId(userId);
        return tasks.stream()
                .map(task -> {
                    String humanReadableCron = cronExpressionTranslator.translate(task.getCronExpression());
                    return ScheduledTaskDTO.fromEntity(task, humanReadableCron);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduledTaskDTO toggleTaskStatus(Long taskId, Long userId) {
        ScheduledTask task = scheduledTaskRepository.findById(taskId)
                .orElseThrow(
                        () -> new jakarta.persistence.EntityNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User does not have permission to modify this task");
        }

        boolean wasActive = task.isActive();

        task.setActive(!wasActive);


        if (!wasActive) {
            logger.info("Task {} has been reactivated. Recalculating next run time.", taskId);

            CronExpression cron = CronExpression.parse(task.getCronExpression());
            LocalDateTime nextRunTime = cron.next(LocalDateTime.now());
            task.setNextRunTime(nextRunTime);

            logger.info("New next run time for task {} is set to {}", taskId, nextRunTime);
        } else {
            logger.info("Task {} has been paused.", taskId);
        }

        ScheduledTask updatedTask = scheduledTaskRepository.save(task);

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

        String cronToSave = updateTaskDTO.getCronExpression();

        if (cronToSave == null || cronToSave.isBlank()) {
            Integer interval = updateTaskDTO.getInterval();
            String unit = updateTaskDTO.getUnit();

            if (interval == null || unit == null) {
                throw new IllegalArgumentException(
                        "Either a cronExpression or both interval and unit must be provided.");
            }

            switch (unit.toLowerCase()) {
                case "minutes":
                    cronToSave = String.format("0 */%d * * * *", interval);
                    break;
                case "hours":
                    cronToSave = String.format("0 0 */%d * * *", interval);
                    break;
                case "days":
                    cronToSave = String.format("0 0 9 */%d * *", interval);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid unit provided. Must be 'minutes', 'hours', or 'days'.");
            }
        }

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