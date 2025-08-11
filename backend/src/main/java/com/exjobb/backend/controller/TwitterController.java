package com.exjobb.backend.controller;

import com.exjobb.backend.entity.User;
import com.exjobb.backend.service.social.TwitterService;
import com.exjobb.backend.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/api/auth/twitter")
public class TwitterController {

    private static final Logger logger = LoggerFactory.getLogger(TwitterController.class);
    @Autowired
    private UserService userService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final TwitterService twitterOAuthService;

    @Value("${twitter.client.id}")
    private String twitterClientId;

    @Value("${twitter.client.secret}")
    private String twitterClientSecret;

    @Value("${twitter.redirect.uri}")
    private String twitterRedirectUri;

    public TwitterController(TwitterService twitterOAuthService) {
        this.twitterOAuthService = twitterOAuthService;
    }

    @GetMapping
    public RedirectView redirectToTwitter(HttpSession session) {
        String platform = "twitter";
        logger.info("Initiating OAuth for platform: {}", platform);

        String state = generateState();
        String sessionKey = "oauthState_" + platform.toLowerCase();

        session.setAttribute(sessionKey, state);
        logger.info("Generated state: '{}', stored with key: '{}'", state, sessionKey);
        logger.info("Session attribute set: {}", session.getAttribute(sessionKey));
        String redirectUrl = twitterOAuthService.generateAuthUrl(state);
        logger.info("Generated redirect URL: {}", redirectUrl);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectUrl);
        return redirectView;
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleTwitterCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            HttpSession session) {

        logger.info("Twitter callback received - code: {}, state: {}, error: {}",
                code != null ? "present" : "null", state, error);
        logger.info("Session ID: {}", session.getId());

        String redirectUrl = twitterOAuthService.handleCallback(code, state, error, error_description, session,
                frontendUrl);
        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTwitterStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : null;

            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }

            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            boolean isConnected = twitterOAuthService.isTwitterConnected(user);

            return ResponseEntity.ok(Map.of(
                    "connected", isConnected,
                    "platform", "twitter"));

        } catch (Exception e) {
            logger.error("Error checking Twitter status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check Twitter status"));
        }
    }

    @PostMapping("/tweet")
    public ResponseEntity<?> postTweet(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : null;

            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }

            String tweetContent = request.get("content");
            if (tweetContent == null || tweetContent.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tweet content cannot be empty"));
            }

            // Twitter has a 280 character limit
            if (tweetContent.length() > 280) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tweet content exceeds 280 character limit"));
            }

            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }


            if (!twitterOAuthService.isTwitterConnected(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Twitter account not connected or token invalid"));
            }

            String tweetId = twitterOAuthService.postTweetForUser(user, tweetContent);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tweetId", tweetId,
                    "message", "Tweet posted successfully"));

        } catch (RuntimeException e) {
            logger.error("Error posting tweet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error posting tweet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to post tweet"));
        }
    }

    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
