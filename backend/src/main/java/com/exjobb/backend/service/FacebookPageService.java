package com.exjobb.backend.service;

import com.exjobb.backend.entity.User;

import com.exjobb.backend.entity.FacebookPageToken;
import com.exjobb.backend.entity.FacebookToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class FacebookPageService {
    private final FacebookTokenService facebookTokenService;
    private final FacebookPageTokenService facebookPageTokenService;

    @Autowired
    public FacebookPageService(FacebookTokenService facebookTokenService, FacebookPageTokenService facebookPageTokenService) {
        this.facebookTokenService = facebookTokenService;
        this.facebookPageTokenService = facebookPageTokenService;
    }

    // 1. Fetch pages and store page tokens
    public List<FacebookPageToken> fetchAndStorePageTokens(User user, String userAccessToken) throws Exception {
        String url = "https://graph.facebook.com/v23.0/me/accounts?access_token=" + userAccessToken;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        List<FacebookPageToken> saved = new ArrayList<>();
        if (root.has("data")) {
            for (JsonNode page : root.get("data")) {
                String pageId = page.get("id").asText();
                String pageName = page.has("name") ? page.get("name").asText() : null;
                String pageAccessToken = page.get("access_token").asText();
                FacebookPageToken token = facebookPageTokenService.savePageToken(user, pageId, pageName, pageAccessToken);
                saved.add(token);
            }
        }
        return saved;
    }

    // 2. Post to a page
    public String postToPage(String pageId, String message) throws Exception {
        FacebookPageToken pageToken = facebookPageTokenService.getTokenByPageId(pageId)
                .orElseThrow(() -> new Exception("Page token not found"));
        String url = "https://graph.facebook.com/v23.0/" + pageId + "/feed";
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.util.MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("message", message);
        params.add("access_token", pageToken.getAccessToken());
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);
            return response.getBody();
        } catch (Exception e) {
            // Log the error and rethrow for easier debugging
            System.err.println("Error posting to Facebook page: " + e.getMessage());
            throw e;
        }
    }
}
