package com.exjobb.backend.service;

import com.exjobb.backend.entity.FacebookPageToken;
import com.exjobb.backend.entity.FacebookToken;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.FacebookPageTokenRepository;
import com.exjobb.backend.repository.FacebookTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FacebookService {

    @Value("${facebook.app.id}")
    private String facebookAppId;

    @Value("${facebook.app.secret}")
    private String facebookAppSecret;

    @Value("${facebook.redirect.uri}")
    private String facebookRedirectUri;

    private final UserService userService;
    private final FacebookTokenRepository facebookTokenRepository;
    private final FacebookPageTokenRepository facebookPageTokenRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public FacebookService(UserService userService, FacebookTokenRepository facebookTokenRepository, FacebookPageTokenRepository facebookPageTokenRepository) {
        this.userService = userService;
        this.facebookTokenRepository = facebookTokenRepository;
        this.facebookPageTokenRepository = facebookPageTokenRepository;
    }

    public String createFacebookAuthorizationUrl() {
        return "https://www.facebook.com/v23.0/dialog/oauth?client_id=" + facebookAppId +
                "&redirect_uri=" + facebookRedirectUri +
                "&scope=email,public_profile,pages_manage_posts,pages_read_engagement,pages_show_list";
    }

    public void handleFacebookCallback(String code, String username) throws Exception {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user_not_found"));

        String userAccessToken = exchangeCodeForAccessToken(code);
        saveUserAccessToken(user, userAccessToken);
        fetchAndStorePageTokens(user, userAccessToken);
    }

    private String exchangeCodeForAccessToken(String code) throws Exception {
        String tokenUrl = "https://graph.facebook.com/v23.0/oauth/access_token"
                + "?client_id=" + facebookAppId
                + "&redirect_uri=" + facebookRedirectUri
                + "&client_secret=" + facebookAppSecret
                + "&code=" + code;

        ResponseEntity<String> response = restTemplate.getForEntity(tokenUrl, String.class);
        JsonNode json = objectMapper.readTree(response.getBody());
        if (json.has("access_token")) {
            return json.get("access_token").asText();
        }
        throw new RuntimeException("facebook_token");
    }

    private void saveUserAccessToken(User user, String accessToken) {
        Optional<FacebookToken> existingToken = facebookTokenRepository.findByUser(user);
        FacebookToken token = existingToken.orElse(new FacebookToken());
        token.setUser(user);
        token.setAccessToken(accessToken);
        // The user access token from the initial auth does not have an expiry if it's a short-lived one.
        // It's better to manage long-lived tokens for server-side usage.
        // For simplicity, we are saving it as is.
        facebookTokenRepository.save(token);
    }

    private void fetchAndStorePageTokens(User user, String userAccessToken) throws Exception {
        String url = "https://graph.facebook.com/v23.0/me/accounts?access_token=" + userAccessToken;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        if (root.has("data")) {
            for (JsonNode pageNode : root.get("data")) {
                String pageId = pageNode.get("id").asText();
                String pageName = pageNode.has("name") ? pageNode.get("name").asText() : null;
                String pageAccessToken = pageNode.get("access_token").asText();
                savePageToken(user, pageId, pageName, pageAccessToken);
            }
        }
    }

    private void savePageToken(User user, String pageId, String pageName, String accessToken) {
        Optional<FacebookPageToken> existing = facebookPageTokenRepository.findByPageId(pageId);
        FacebookPageToken token = existing.orElse(new FacebookPageToken());
        token.setUser(user);
        token.setPageId(pageId);
        token.setPageName(pageName);
        token.setAccessToken(accessToken);
        facebookPageTokenRepository.save(token);
    }

    public String postToUserFirstPage(String username, String message) throws Exception {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<FacebookPageToken> pages = facebookPageTokenRepository.findAllByUser(user);
        if (pages.isEmpty()) {
            throw new IllegalStateException("No Facebook pages found for this user");
        }
        if (pages.size() > 1) {
            throw new IllegalStateException("Multiple Facebook pages found. Please specify a pageId.");
        }

        String userAccessToken = facebookTokenRepository.findByUser(user)
                .map(FacebookToken::getAccessToken)
                .orElseThrow(() -> new RuntimeException("Facebook access token not found."));

        if (!isTokenValid(userAccessToken)) {
            throw new RuntimeException("Facebook access token expired or invalid. Please reconnect your Facebook account.");
        }

        return postToPage(pages.get(0).getPageId(), message);
    }

    private String postToPage(String pageId, String message) throws Exception {
        FacebookPageToken pageToken = facebookPageTokenRepository.findByPageId(pageId)
                .orElseThrow(() -> new Exception("Page token not found"));

        String url = "https://graph.facebook.com/v23.0/" + pageId + "/feed";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("message", message);
        params.add("access_token", pageToken.getAccessToken());

        ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);
        return response.getBody();
    }

    private boolean isTokenValid(String accessToken) {
        String debugUrl = "https://graph.facebook.com/debug_token?input_token=" + accessToken +
                "&access_token=" + facebookAppId + "%7C" + facebookAppSecret;
        try {
            ResponseEntity<String> debugResponse = restTemplate.getForEntity(debugUrl, String.class);
            JsonNode debugJson = objectMapper.readTree(debugResponse.getBody());
            JsonNode data = debugJson.get("data");
            return data != null && data.has("is_valid") && data.get("is_valid").asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> checkConnectionStatus(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<FacebookToken> tokenOpt = facebookTokenRepository.findByUser(user);
        if (tokenOpt.isEmpty()) {
            return Map.of("connected", false, "reason", "No token");
        }

        String userAccessToken = tokenOpt.get().getAccessToken();
        String debugUrl = "https://graph.facebook.com/debug_token?input_token=" + userAccessToken +
                "&access_token=" + facebookAppId + "%7C" + facebookAppSecret;
        try {
            ResponseEntity<String> debugResponse = restTemplate.getForEntity(debugUrl, String.class);
            JsonNode debugJson = objectMapper.readTree(debugResponse.getBody());
            JsonNode data = debugJson.get("data");

            boolean tokenValid = data != null && data.has("is_valid") && data.get("is_valid").asBoolean();
            Long expiresAt = (data != null && data.has("expires_at")) ? data.get("expires_at").asLong() : null;

            return Map.of("connected", tokenValid, "expiresAt", expiresAt);
        } catch (Exception e) {
            return Map.of("connected", false, "reason", "Failed to validate token");
        }
    }
}