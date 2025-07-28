package com.exjobb.backend.repository;

import com.exjobb.backend.entity.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    // This method find all active tasks which are ready to be run
    List<ScheduledTask> findByIsActiveTrueAndNextRunTimeBefore(LocalDateTime now);

    List<ScheduledTask> findByUserId(Long userId);
}
