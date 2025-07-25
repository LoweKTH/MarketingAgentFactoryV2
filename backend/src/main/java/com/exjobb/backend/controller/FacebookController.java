package com.exjobb.backend.controller;

import com.exjobb.backend.service.FacebookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/api/auth/facebook")
public class FacebookController {

    @Autowired
    private FacebookService facebookService;

    @GetMapping
    public RedirectView redirectToFacebook() {
        return new RedirectView(facebookService.createFacebookAuthorizationUrl());
    }

    @GetMapping("/callback")
    public String handleFacebookCallback(@RequestParam(name = "code", required = false) String code,
                                         @RequestParam(name = "error", required = false) String error) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;

        if (username == null || "anonymousUser".equals(username)) {
            return "redirect:http://localhost:5173/login?error=not_authenticated";
        }

        if (error != null) {
            return "redirect:http://localhost:5173/login?error=facebook_denied";
        }

        if (code != null) {
            try {
                facebookService.handleFacebookCallback(code, username);
                return "redirect:http://localhost:5173/profile?facebook=connected";
            } catch (Exception e) {
                // Log the exception e.g., e.printStackTrace();
                return "redirect:http://localhost:5173/login?error=" + e.getMessage();
            }
        }

        return "redirect:http://localhost:5173/login?error=facebook_unknown";
    }

    @GetMapping("/post-to-page")
    public ResponseEntity<?> postToOnlyPage(@RequestParam String message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;

        if (username == null || "anonymousUser".equals(username)) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        try {
            String result = facebookService.postToUserFirstPage(username, message);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getFacebookConnectionStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;

        if (username == null || "anonymousUser".equals(username)) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        try {
            return ResponseEntity.ok(facebookService.checkConnectionStatus(username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to check Facebook connection status.");
        }
    }
}