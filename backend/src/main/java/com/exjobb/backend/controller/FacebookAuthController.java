package com.exjobb.backend.controller;

import com.exjobb.backend.entity.User;
import com.exjobb.backend.service.UserService;
import com.exjobb.backend.service.FacebookTokenService;
import com.exjobb.backend.service.FacebookPageService;
import com.exjobb.backend.service.FacebookPageTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/api/auth/facebook")
public class FacebookAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private FacebookTokenService facebookTokenService;
    @Autowired
    private FacebookPageService facebookPageService;
    @Autowired
    private FacebookPageTokenService facebookPageTokenService;

    @Value("${facebook.app.id}")
    private String facebookAppId;


    @Value("${facebook.app.secret}")
    private String facebookAppSecret;

    @Value("${facebook.redirect.uri}")
    private String facebookRedirectUri;

    @GetMapping
    public RedirectView redirectToFacebook() {
        String facebookOauthUrl = "https://www.facebook.com/v23.0/dialog/oauth?client_id="
                + facebookAppId + "&redirect_uri=" + facebookRedirectUri +
                "&scope=email,public_profile,pages_manage_posts,pages_read_engagement,pages_show_list";
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(facebookOauthUrl);
        return redirectView;
    }

    @GetMapping("/callback")
    public String handleFacebookCallback(@RequestParam(name = "code", required = false) String code,
                                         @RequestParam(name = "error", required = false) String error) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || username.equals("anonymousUser")) {
            return "redirect:http://localhost:5173/login?error=not_authenticated";
        }
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:http://localhost:5173/login?error=user_not_found";
        }
        if (error != null) {
            // Handle error (e.g., user denied access)
            return "redirect:http://localhost:5173/login?error=facebook_denied";
        }
        if (code != null) {
            // Step 1: Exchange code for access token
            String tokenUrl = "https://graph.facebook.com/v23.0/oauth/access_token"
                    + "?client_id=" + facebookAppId
                    + "&redirect_uri=" + facebookRedirectUri
                    + "&client_secret=" + facebookAppSecret
                    + "&code=" + code;
            System.out.println(tokenUrl);
            RestTemplate restTemplate = new RestTemplate();
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(tokenUrl, String.class);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.getBody());
                System.out.println(json);
                String accessToken = json.get("access_token").asText();
                Long expiresIn = json.has("expires_in") ? json.get("expires_in").asLong() : null;
                System.out.println(expiresIn);
                String tokenType = json.has("token_type") ? json.get("token_type").asText() : null;


                // Store user access token
                facebookTokenService.saveToken(user, accessToken, expiresIn, tokenType);

                // Step 2: Use access token to fetch and store page tokens
                try {
                    facebookPageService.fetchAndStorePageTokens(user, accessToken);
                } catch (Exception e) {
                    // Log or handle error, but don't block user login
                }

                return "redirect:http://localhost:5173/profile?facebook=connected";
            } catch (Exception e) {
                // Handle error in token exchange
                return "redirect:http://localhost:5173/login?error=facebook_token";
            }
        }
        return "redirect:http://localhost:5173/login?error=facebook_unknown";
    }

    // Endpoint to post to the user's only page (if only one exists)
    @GetMapping("/post-to-page")
    public ResponseEntity<?> postToOnlyPage(@RequestParam String message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || username.equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        var pages = facebookPageTokenService.getTokensByUser(user);
        if (pages == null || pages.isEmpty()) {
            return ResponseEntity.badRequest().body("No Facebook pages found for this user");
        }
        if (pages.size() > 1) {
            return ResponseEntity.badRequest().body("Multiple Facebook pages found. Please specify a pageId.");
        }
        String pageId = pages.get(0).getPageId();
        // Check if the user's access token is valid using /debug_token
        String userAccessToken = facebookTokenService.getTokenByUser(user)
                .map(t -> t.getAccessToken()).orElse(null);
        boolean tokenValid = false;
        if (userAccessToken != null) {
            try {
                String debugUrl = "https://graph.facebook.com/debug_token?input_token=" + userAccessToken +
                        "&access_token=" + facebookAppId + "%7C" + facebookAppSecret;
                RestTemplate restTemplate = new RestTemplate();
                ObjectMapper mapper = new ObjectMapper();
                ResponseEntity<String> debugResponse = restTemplate.getForEntity(debugUrl, String.class);
                JsonNode debugJson = mapper.readTree(debugResponse.getBody());
                JsonNode data = debugJson.get("data");
                tokenValid = data != null && data.has("is_valid") && data.get("is_valid").asBoolean();
            } catch (Exception e) {
                System.err.println("Failed to check /debug_token before posting: " + e.getMessage());
            }
        }
        // If not valid, try to renew the token (by redirecting user to Facebook OAuth)
        if (!tokenValid) {
            // Optionally, you could trigger a refresh flow here. For now, return a clear error.
            return ResponseEntity.status(401).body("Facebook access token expired or invalid. Please reconnect your Facebook account.");
        }
        try {
            String result = facebookPageService.postToPage(pageId, message);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Endpoint to check Facebook connection status
    @GetMapping("/status")
    public ResponseEntity<?> getFacebookConnectionStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || username.equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        String userAccessToken = facebookTokenService.getTokenByUser(user)
                .map(t -> t.getAccessToken()).orElse(null);
        if (userAccessToken == null) {
            return ResponseEntity.ok(java.util.Map.of("connected", false, "reason", "No token"));
        }
        boolean tokenValid = false;
        Long expiresAt = null;
        try {
            String debugUrl = "https://graph.facebook.com/debug_token?input_token=" + userAccessToken +
                    "&access_token=" + facebookAppId + "%7C" + facebookAppSecret;
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();
            ResponseEntity<String> debugResponse = restTemplate.getForEntity(debugUrl, String.class);
            JsonNode debugJson = mapper.readTree(debugResponse.getBody());
            JsonNode data = debugJson.get("data");
            tokenValid = data != null && data.has("is_valid") && data.get("is_valid").asBoolean();
            if (data != null && data.has("expires_at")) {
                expiresAt = data.get("expires_at").asLong();
            }
        } catch (Exception e) {
            System.err.println("Failed to check /debug_token for status: " + e.getMessage());
        }
        return ResponseEntity.ok(java.util.Map.of(
                "connected", tokenValid,
                "expiresAt", expiresAt
        ));
    }

}
