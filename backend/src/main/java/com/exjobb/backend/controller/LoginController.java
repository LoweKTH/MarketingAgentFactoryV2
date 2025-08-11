package com.exjobb.backend.controller;

import com.exjobb.backend.dto.LoginRequest;
import com.exjobb.backend.dto.LoginResponse;
import com.exjobb.backend.service.user.UserService;
import com.exjobb.backend.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Validated
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public LoginController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);

            LoginResponse response = new LoginResponse(
                    jwt,
                    "Login successful for user: " + loginRequest.getUsername()
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            LoginResponse response = new LoginResponse(
                    null,
                    "Invalid username or password"
            );

            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logout successful");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            Map<String, Object> response = new HashMap<>();
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            response.put("authenticated", true);

            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);

            return ResponseEntity.status(401).body(response);
        }
    }
}