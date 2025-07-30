package com.exjobb.backend.service;


import com.exjobb.backend.dto.ChatMessageRequest;
import com.exjobb.backend.entity.ScheduledTask;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.ScheduledTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgentSchedulingService {

    private static final Logger logger = LoggerFactory.getLogger(AgentSchedulingService.class);
    private final AgentService agentService;
    private final ScheduledTaskRepository scheduledTaskRepository;

    public AgentSchedulingService(AgentService agentService,
                                  ScheduledTaskRepository scheduledTaskRepository) {
        this.agentService = agentService;
        this.scheduledTaskRepository = scheduledTaskRepository;
    }

    // Run every minute to see if any tasks should be started
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void executeDueTasks(){
        logger.info("--- SCHEDULER: Checking for due tasks... ---");

        List<ScheduledTask> dueTasks = scheduledTaskRepository
                .findByIsActiveTrueAndNextRunTimeBefore(java.time.LocalDateTime.now());

        if(dueTasks.isEmpty()){
            logger.info("--- SCHEDULER: No tasks are due.");
            return;
        }
        for(ScheduledTask task : dueTasks){
            logger.info("--- SCHEDULER: Executing task ID: {} ---", task.getId());

            // Create request and run agent with prompt from database
            ChatMessageRequest request = new ChatMessageRequest(task.getPrompt(), null);
            agentService.executeStandaloneTask(task.getPrompt(), task.getUser());

            // Update task for next run
            task.setLastRunTime(LocalDateTime.now());

            // Calculate next run time based on cron-expression
            CronExpression cron = CronExpression.parse(task.getCronExpression());
            LocalDateTime nextRunTime = cron.next(LocalDateTime.now());
            task.setNextRunTime(nextRunTime);

            scheduledTaskRepository.save(task);
            logger.info("--- SCHEDULER: Task ID {} finished and rescheduled for {}. ---",
                    task.getId(), nextRunTime);
        }


    }
}
