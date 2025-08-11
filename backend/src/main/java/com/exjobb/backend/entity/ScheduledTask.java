package com.exjobb.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
public class ScheduledTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false)
    private String cronExpression;

    private LocalDateTime nextRunTime;

    private LocalDateTime lastRunTime;

    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ScheduledTask(String prompt, String cronExpression, User user) {
        this.prompt = prompt;
        this.cronExpression = cronExpression;
        this.user = user;
    }



}
