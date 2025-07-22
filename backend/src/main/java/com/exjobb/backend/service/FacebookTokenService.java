package com.exjobb.backend.service;

import com.exjobb.backend.entity.FacebookToken;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.FacebookTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FacebookTokenService {
    private final FacebookTokenRepository facebookTokenRepository;

    @Autowired
    public FacebookTokenService(FacebookTokenRepository facebookTokenRepository) {
        this.facebookTokenRepository = facebookTokenRepository;
    }

    public FacebookToken saveToken(User user, String accessToken, Long expiresIn, String tokenType) {
        Optional<FacebookToken> existing = facebookTokenRepository.findByUser(user);
        FacebookToken token = existing.orElse(new FacebookToken());
        token.setUser(user);
        token.setAccessToken(accessToken);
        token.setExpiresIn(expiresIn);
        token.setTokenType(tokenType);
        return facebookTokenRepository.save(token);
    }

    public Optional<FacebookToken> getTokenByUser(User user) {
        return facebookTokenRepository.findByUser(user);
    }
}
