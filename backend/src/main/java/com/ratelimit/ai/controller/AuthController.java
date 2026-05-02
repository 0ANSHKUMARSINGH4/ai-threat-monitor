package com.ratelimit.ai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (adminUsername.equals(username) && adminPassword.equals(password)) {
                String sessionToken = UUID.randomUUID().toString();
                return ResponseEntity.ok()
                    .body(Map.of(
                        "message", "Login successful",
                        "token", sessionToken
                    ));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
                    
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of(
                        "error", e.getMessage() != null ? e.getMessage() : "Internal Server Error"
                    ));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("SESSION_TOKEN", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) 
                .sameSite("None")
                .build();
                
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out"));
    }
}
