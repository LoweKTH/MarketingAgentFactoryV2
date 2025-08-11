package com.exjobb.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class TwitterToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String accessToken;
    private String refreshToken;
    private String expiresIn;
    private Instant createdAt;

    public TwitterToken() {}

    public TwitterToken(User user, String accessToken, String refreshToken, String expiresIn) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.createdAt = Instant.now();
    }


    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getExpiresIn() { return expiresIn; }
    public void setExpiresIn(String expiresIn) { this.expiresIn = expiresIn; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
