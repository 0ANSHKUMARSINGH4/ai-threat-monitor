package com.ratelimit.ai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class DummyApiController {

    @GetMapping("/public/data")
    public Map<String, String> getPublicData() {
        Map<String, String> data = new HashMap<>();
        data.put("message", "Here is some public data.");
        return data;
    }

    @PostMapping("/auth/login")
    public Map<String, String> login() {
        // Simulating a login endpoint where attackers might brute-force
        Map<String, String> data = new HashMap<>();
        data.put("status", "success");
        return data;
    }

    @GetMapping("/user/profile")
    public Map<String, String> userProfile() {
        Map<String, String> data = new HashMap<>();
        data.put("user", "Alice");
        data.put("role", "USER");
        return data;
    }
}
