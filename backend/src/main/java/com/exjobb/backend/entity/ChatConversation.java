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
public class ChatConversation {


    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String title;
    private LocalDateTime creationTimeStamp;

    @PrePersist
    protected void onCreate(){
        this.creationTimeStamp = LocalDateTime.now();
    }
}
