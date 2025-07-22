
package com.exjobb.backend.service;

import com.exjobb.backend.entity.User;

import com.exjobb.backend.entity.FacebookPageToken;
import com.exjobb.backend.repository.FacebookPageTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FacebookPageTokenService {
    private final FacebookPageTokenRepository facebookPageTokenRepository;

    @Autowired
    public FacebookPageTokenService(FacebookPageTokenRepository facebookPageTokenRepository) {
        this.facebookPageTokenRepository = facebookPageTokenRepository;
    }

    public FacebookPageToken savePageToken(User user, String pageId, String pageName, String accessToken) {
        Optional<FacebookPageToken> existing = facebookPageTokenRepository.findByPageId(pageId);
        FacebookPageToken token = existing.orElse(new FacebookPageToken());
        token.setUser(user);
        token.setPageId(pageId);
        token.setPageName(pageName);
        token.setAccessToken(accessToken);
        return facebookPageTokenRepository.save(token);
    }

    public Optional<FacebookPageToken> getTokenByPageId(String pageId) {
        return facebookPageTokenRepository.findByPageId(pageId);
    }

    public java.util.List<FacebookPageToken> getTokensByUser(User user) {
        return facebookPageTokenRepository.findAllByUser(user);
    }
}
