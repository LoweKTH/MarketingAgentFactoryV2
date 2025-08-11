package com.exjobb.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SocialMediaPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String content;
    private String platform;
    private LocalDateTime creationTimeStamp;
    private LocalDateTime lastModified;
    private Double engagementScore;
    private Boolean isApprovedByUser;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate(){
        this.creationTimeStamp = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        this.lastModified = LocalDateTime.now();
    }
}
