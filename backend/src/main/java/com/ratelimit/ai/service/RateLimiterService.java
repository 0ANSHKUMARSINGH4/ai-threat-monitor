package com.ratelimit.ai.service;

import com.ratelimit.ai.repository.AiClassificationRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final AiClassificationRepository aiRepo;

    public RateLimiterService(StringRedisTemplate redisTemplate, AiClassificationRepository aiRepo) {
        this.redisTemplate = redisTemplate;
        this.aiRepo = aiRepo;
    }

    public boolean isAllowed(String ipAddress, String uri) {
        // 0. Hard Bypassing for System Critical Endpoints
        if (uri != null && (uri.startsWith("/admin") || uri.startsWith("/health") || uri.startsWith("/error"))) {
            return true;
        }

        // 1. Check AI Block (Cached in Redis for 30s)
        String aiStatusKey = "ai:status:" + ipAddress;
        String status = redisTemplate.opsForValue().get(aiStatusKey);

        if (status == null) {
            var classificationOpt = aiRepo.findTopByIpAddressOrderByEvaluatedAtDesc(ipAddress);
            status = classificationOpt
                .filter(c -> c.getEvaluatedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(2)))
                .map(com.ratelimit.ai.domain.AiClassification::getClassification)
                .orElse("LEGITIMATE");
            redisTemplate.opsForValue().set(aiStatusKey, status, 30, TimeUnit.SECONDS);
        }

        if ("ABUSIVE".equalsIgnoreCase(status)) {
            return false; 
        }

        // 2. Redis Rate Limiting 
        String key = "rate:" + ipAddress + ":" + (System.currentTimeMillis() / 60000);
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count != null && count == 1L) {
            redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }

        int maxAllowed = "SUSPICIOUS".equalsIgnoreCase(status) ? 20 : 200; 

        return count != null && count <= maxAllowed;
    }
}
