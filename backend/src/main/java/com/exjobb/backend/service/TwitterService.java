package com.exjobb.backend.service;

import com.exjobb.backend.entity.TwitterToken;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.TwitterTokenRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service("twitterOAuthService")
public class TwitterService {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(TwitterService.class);

    @Value("${twitter.client.id}")
    private String clientId;

    @Value("${twitter.client.secret}")
    private String clientSecret;

    @Value("${twitter.redirect.uri}")
    private String redirectUri;

    @Value("${twitter.oauth.token-url}")
    private String tokenUrl;

    @Value("${twitter.api.users-me-url}")
    private String usersMeUrl;

    @Value("${twitter.api.tweets-url}")
    private String tweetsUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String codeChallenge = "challenge";
    private final String codeChallengeMethod = "plain";

    private final TwitterTokenRepository twitterTokenRepository;

    public TwitterService(TwitterTokenRepository twitterTokenRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.twitterTokenRepository = twitterTokenRepository;
    }

    public String generateAuthUrl(String state) {
        try {
            String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString());
            String scope = URLEncoder.encode("tweet.read tweet.write users.read offline.access",
                    StandardCharsets.UTF_8.toString());

            return "https://x.com/i/oauth2/authorize"
                    + "?response_type=code"
                    + "&client_id=" + clientId
                    + "&redirect_uri=" + encodedRedirectUri
                    + "&scope=" + scope
                    + "&state=" + state
                    + "&code_challenge=" + codeChallenge
                    + "&code_challenge_method=" + codeChallengeMethod;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Twitter auth URL", e);
        }
    }

    public String handleCallback(String code, String state, String error, String error_description, HttpSession session,
                                 String frontendUrl) {
        String frontendRedirectBaseUrl = frontendUrl + "/profile";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("All session attributes in service: ");
        session.getAttributeNames().asIterator()
                .forEachRemaining(attr -> logger.info("   {}: {}", attr, session.getAttribute(attr)));

        if (error != null) {
            logger.error("Twitter OAuth error: {} - {}", error, error_description);
            String errorRedirectUrl = frontendRedirectBaseUrl + "?error="
                    + URLEncoder.encode(error, StandardCharsets.UTF_8);
            if (error_description != null) {
                errorRedirectUrl += "&error_description="
                        + URLEncoder.encode(error_description, StandardCharsets.UTF_8);
            }
            return errorRedirectUrl;
        }

        String expectedState = (String) session.getAttribute("oauthState_twitter");
        logger.info("Expected state: '{}', Received state: '{}'", expectedState, state);

        if (expectedState == null || !expectedState.equals(state)) {
            logger.error("Invalid state parameter. Expected: '{}', Received: '{}'", expectedState, state);
            String errorRedirectUrl = frontendRedirectBaseUrl + "?error=invalid_state&error_description=" +
                    URLEncoder.encode("Invalid state parameter - Expected: " + expectedState + ", Received: " + state,
                            StandardCharsets.UTF_8);
            return errorRedirectUrl;
        }

        if (code == null || code.isEmpty()) {
            logger.error("No authorization code received");
            String errorRedirectUrl = frontendRedirectBaseUrl + "?error=no_code&error_description=" +
                    URLEncoder.encode("No authorization code received", StandardCharsets.UTF_8);
            return errorRedirectUrl;
        }

        session.removeAttribute("oauthState_twitter");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = clientId + ":" + clientSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
            headers.set("Authorization", authHeader);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", code);
            body.add("grant_type", "authorization_code");
            body.add("client_id", clientId);
            body.add("redirect_uri", redirectUri);
            body.add("code_verifier", codeChallenge);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    String.class);
            System.out.println("test1: " + responseEntity);

            AccessTokenResponse tokenResponse = null;

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    tokenResponse = objectMapper.readValue(responseEntity.getBody(), AccessTokenResponse.class);
                } catch (Exception e) {
                    logger.error("Failed to parse access token response: {}", e.getMessage(), e);
                    return frontendRedirectBaseUrl + "?error=json_parse_failed&error_description=" +
                            URLEncoder.encode("Failed to parse access token response", StandardCharsets.UTF_8);
                }
            } else {
                logger.error("Failed to obtain access token. Status: {}, Body: {}",
                        responseEntity.getStatusCode(), responseEntity.getBody());
                return frontendRedirectBaseUrl + "?error=token_exchange_failed&error_description=" +
                        URLEncoder.encode("Failed to exchange authorization code for access token. " +
                                        "Server responded with status: " + responseEntity.getStatusCode(),
                                StandardCharsets.UTF_8);
            }

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                logger.info("Successfully obtained Twitter access token!");
                logger.debug("Access Token: {}", tokenResponse.getAccessToken());



                String username = authentication != null ? authentication.getName() : null;
                User user = userService.findByUsername(username).orElse(null);
                Optional<TwitterToken> existing = twitterTokenRepository.findByUser(user);
                TwitterToken token = existing.orElse(new TwitterToken());
                token.setUser(user);
                token.setAccessToken(tokenResponse.getAccessToken());
                token.setRefreshToken(tokenResponse.getRefreshToken());
                token.setExpiresIn(tokenResponse.getExpiresIn());
                token.setCreatedAt(java.time.Instant.now());
                twitterTokenRepository.save(token);

                return frontendRedirectBaseUrl + "?success=true&platform=twitter";

            } else {
                logger.error("Failed to obtain access token: {}", tokenResponse);
                return frontendRedirectBaseUrl + "?error=token_exchange_failed&error_description=" +
                        URLEncoder.encode("Failed to exchange authorization code for access token",
                                StandardCharsets.UTF_8);
            }

        } catch (Exception e) {
            logger.error("Error during access token exchange or saving: {}", e.getMessage(), e);
            return frontendRedirectBaseUrl + "?error=token_exchange_exception&error_description=" +
                    URLEncoder.encode("An error occurred during access token exchange: " + e.getMessage(),
                            StandardCharsets.UTF_8);
        }
    }

    private String fetchTwitterUserId(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    usersMeUrl,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                if (responseMap.containsKey("data")) {
                    Map<String, String> userData = (Map<String, String>) responseMap.get("data");
                    if (userData.containsKey("id")) {
                        String userId = userData.get("id");
                        logger.info("Successfully fetched Twitter user ID: {}", userId);
                        return userId;
                    }
                }
            }
            logger.error("Failed to fetch Twitter user ID. Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            return null;
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Unauthorized to fetch Twitter user ID. Access token might be invalid or expired: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching Twitter user ID: {}", e.getMessage(), e);
            return null;
        }
    }


    /**
     * Refreshes the Twitter access token using the provided refresh token.
     *
     * @param currentRefreshToken The refresh token obtained during initial OAuth.
     * @return A new AccessTokenResponse if successful, or null if refresh fails.
     */
    public AccessTokenResponse refreshAccessToken(String currentRefreshToken) {
        logger.info("Attempting to refresh Twitter access token using refresh token.");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = clientId + ":" + clientSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
            headers.set("Authorization", authHeader);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", currentRefreshToken);
            body.add("client_id", clientId); // Required for public clients and sometimes for confidential clients

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                AccessTokenResponse newTokenResponse = objectMapper.readValue(responseEntity.getBody(), AccessTokenResponse.class);
                logger.info("Successfully refreshed Twitter access token.");
                return newTokenResponse;
            } else {
                logger.error("Failed to refresh access token. Status: {}, Body: {}",
                        responseEntity.getStatusCode(), responseEntity.getBody());
                return null;
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Refresh token is invalid or expired (Unauthorized): {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error during access token refresh: {}", e.getMessage(), e);
            return null;
        }

    }

    /**
     * Checks if the Twitter access token for a given user is still valid.
     * If it's expired, it attempts to refresh it using the refresh token.
     *
     * @param user The user whose Twitter token is to be checked and potentially refreshed.
     * @return The updated TwitterToken object if valid or successfully refreshed,
     * or an empty Optional if the token is invalid and cannot be refreshed.
     */
    public Optional<TwitterToken> checkAndRefreshToken(User user) {
        logger.debug("Checking Twitter token validity for user: {}", user.getUsername());
        Optional<TwitterToken> tokenOptional = twitterTokenRepository.findByUser(user);

        if (tokenOptional.isEmpty()) {
            logger.info("No Twitter token found for user: {}", user.getUsername());
            return Optional.empty();
        }

        TwitterToken token = tokenOptional.get();
        // Calculate expiration time based on creation time and expiresIn duration
        // Assuming expiresIn is in seconds
        java.time.Instant expiresAt = token.getCreatedAt().plusSeconds(Long.parseLong(token.getExpiresIn()));

        logger.debug("Token created at: {}", token.getCreatedAt());
        logger.debug("Token expires at: {}", expiresAt);
        logger.debug("Current time: {}", java.time.Instant.now());
        // Check if the token is expired or about to expire (e.g., within 5 minutes)
        // Add a small buffer (e.g., 300 seconds = 5 minutes) to refresh proactively
        if (Instant.now().isAfter(expiresAt.minusSeconds(300))) {
            logger.info("Twitter token for user {} is expired or near expiration. Attempting to refresh.", user.getUsername());

            if (token.getRefreshToken() != null && !token.getRefreshToken().isEmpty()) {
                AccessTokenResponse refreshedTokenResponse = refreshAccessToken(token.getRefreshToken());

                if (refreshedTokenResponse != null && refreshedTokenResponse.getAccessToken() != null) {
                    // Update the stored token with new values
                    token.setAccessToken(refreshedTokenResponse.getAccessToken());
                    // Refresh token might also be refreshed, Twitter API v2 often provides new refresh token
                    if (refreshedTokenResponse.getRefreshToken() != null) {
                        token.setRefreshToken(refreshedTokenResponse.getRefreshToken());
                    }
                    token.setExpiresIn(refreshedTokenResponse.getExpiresIn());
                    token.setCreatedAt(Instant.now()); // Update creation time to now
                    twitterTokenRepository.save(token);
                    logger.info("Twitter token successfully refreshed and saved for user: {}", user.getUsername());
                    return Optional.of(token);
                } else {
                    logger.warn("Failed to refresh Twitter token for user {}. Refresh token might be invalid or expired.", user.getUsername());
                    // Invalidate the token if refresh failed
                    twitterTokenRepository.delete(token);
                    return Optional.empty();
                }
            } else {
                logger.warn("No refresh token available for user {}. Twitter token cannot be refreshed.", user.getUsername());
                // Invalidate the token as it cannot be refreshed
                twitterTokenRepository.delete(token);
                return Optional.empty();
            }
        } else {
            logger.debug("Twitter token for user {} is still valid.", user.getUsername());
            return Optional.of(token);
        }
    }


    /**
     * Posts a tweet for a specific user, handling token refresh if needed.
     *
     * @param user         The user to post the tweet for
     * @param tweetContent The content of the tweet
     * @return The tweet ID if successful
     * @throws RuntimeException if posting fails
     */
    public String postTweetForUser(User user, String tweetContent) {
        logger.info("Attempting to post tweet for user: {}", user.getUsername());

        // Call the new checkAndRefreshToken method
        Optional<TwitterToken> tokenOptional = checkAndRefreshToken(user);

        if (tokenOptional.isEmpty()) {
            throw new RuntimeException("No valid or refreshable Twitter token found for user. Please reconnect your Twitter account.");
        }

        TwitterToken token = tokenOptional.get();
        String accessToken = token.getAccessToken();

        // The token should be valid at this point or an exception would have been thrown
        return postTweet(accessToken, tweetContent);
    }

    // Enhanced version of your existing postTweet method with better error handling
    public String postTweet(String accessToken, String tweetContent) {
        logger.info("Attempting to publish tweet to Twitter.");
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Twitter API v2 endpoint for posting tweets
            Map<String, String> requestBody = Collections.singletonMap("text", tweetContent);
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    tweetsUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                // Parse the response to extract the tweet ID
                Map<String, Object> responseMap = objectMapper.readValue(responseEntity.getBody(), Map.class);
                if (responseMap.containsKey("data")) {
                    Map<String, String> data = (Map<String, String>) responseMap.get("data");
                    if (data.containsKey("id")) {
                        String tweetId = data.get("id");
                        logger.info("Tweet published successfully. Tweet ID: {}", tweetId);
                        return tweetId;
                    }
                }
                logger.warn("Tweet published successfully but could not retrieve tweet ID from response: {}",
                        responseEntity.getBody());
                return "UNKNOWN_ID";
            } else {
                logger.error("Failed to publish tweet. Status: {}, Body: {}", responseEntity.getStatusCode(),
                        responseEntity.getBody());
                throw new RuntimeException("Failed to publish tweet: HTTP " + responseEntity.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            logger.error("HTTP error publishing tweet: Status: {}, Body: {}", e.getStatusCode(), errorBody, e);

            // Parse Twitter API error response for better error messages
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(errorBody, Map.class);
                if (errorResponse.containsKey("errors")) {
                    List<Map<String, Object>> errors = (List<Map<String, Object>>) errorResponse.get("errors");
                    if (!errors.isEmpty()) {
                        String errorMessage = (String) errors.get(0).get("message");
                        throw new RuntimeException("Twitter API error: " + errorMessage);
                    }
                }
            } catch (Exception parseException) {
                // Fall back to generic error if we can't parse the response
            }

            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("Unauthorized - Twitter token may be invalid or expired");
            } else if (e.getStatusCode().value() == 403) {
                throw new RuntimeException("Forbidden - Check Twitter API permissions or account status");
            } else {
                throw new RuntimeException("HTTP error publishing tweet: " + e.getStatusCode() + " - " + errorBody);
            }
        } catch (Exception e) {
            logger.error("An unexpected error occurred while publishing tweet: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during tweet publication: " + e.getMessage());
        }
    }

    // Add this method to your TwitterOAuthService class
     public boolean isTwitterConnected(User user) {
        if (user == null) {
            return false;
        }
        
        Optional<TwitterToken> twitterTokenOptional = checkAndRefreshToken(user); // Use your existing method

        // If checkAndRefreshToken returns an Optional with a token, it means it's valid or successfully refreshed.
        // Then, check if the accessToken itself is present and not empty.
        return twitterTokenOptional.isPresent() 
               && twitterTokenOptional.get().getAccessToken() != null 
               && !twitterTokenOptional.get().getAccessToken().isEmpty();
    }

    public static class AccessTokenResponse {
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("expires_in")
        private String expiresIn;
        @JsonProperty("access_token")
        private String accessToken;

        private String scope;

        @JsonProperty("refresh_token")
        private String refreshToken;

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(String expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        @Override
        public String toString() {
            return "AccessTokenResponse{" +
                    "tokenType='" + tokenType + '\'' +
                    ", expiresIn='" + expiresIn + '\'' +
                    ", accessToken='" + (accessToken != null ? "[REDACTED]" : "null") + '\'' +
                    ", scope='" + scope + '\'' +
                    ", refreshToken='" + (refreshToken != null ? "[REDACTED]" : "null") + '\'' +
                    '}';
        }
    }
}