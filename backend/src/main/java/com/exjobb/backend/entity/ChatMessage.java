package com.exjobb.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String message;
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    private ChatConversation conversation;
    private LocalDateTime creationTimeStamp;

    @PrePersist
    protected void onCreate(){
        this.creationTimeStamp = LocalDateTime.now();
    }
}
