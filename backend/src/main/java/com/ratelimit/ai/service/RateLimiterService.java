package com.ratelimit.ai.service;

import com.ratelimit.ai.repository.AiClassificationRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final AiClassificationRepository aiRepo;
    private final DefaultRedisScript<Long> slidingWindowScript;

    public RateLimiterService(StringRedisTemplate redisTemplate, AiClassificationRepository aiRepo) {
        this.redisTemplate = redisTemplate;
        this.aiRepo = aiRepo;
        
        String script = 
            "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, tonumber(ARGV[1]) - tonumber(ARGV[2]));\n" +
            "local count = redis.call('ZCARD', KEYS[1]);\n" +
            "if tonumber(count) < tonumber(ARGV[3]) then\n" +
            "    redis.call('ZADD', KEYS[1], ARGV[1], ARGV[4]);\n" +
            "    if tonumber(count) == 0 then\n" +
            "        redis.call('EXPIRE', KEYS[1], math.ceil(tonumber(ARGV[2]) / 1000));\n" +
            "    end;\n" +
            "    return count + 1;\n" +
            "end;\n" +
            "return count;";
            
        this.slidingWindowScript = new DefaultRedisScript<>(script, Long.class);
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

        // 2. Redis Sliding Window Rate Limiting (Atomic Lua Script)
        int maxAllowed = "SUSPICIOUS".equalsIgnoreCase(status) ? 20 : 200; 
        String key = "window:" + ipAddress;
        long now = System.currentTimeMillis();
        long windowSizeMs = 60000; // 60 seconds
        String requestId = UUID.randomUUID().toString();

        Long currentCount = redisTemplate.execute(
            slidingWindowScript,
            List.of(key),
            String.valueOf(now),
            String.valueOf(windowSizeMs),
            String.valueOf(maxAllowed),
            requestId
        );

        if (currentCount != null && currentCount <= maxAllowed) {
            String endpointKey = "endpoints:" + ipAddress;
            redisTemplate.opsForSet().add(endpointKey, uri != null ? uri : "/");
            redisTemplate.expire(endpointKey, 60, TimeUnit.SECONDS);
        }

        return currentCount != null && currentCount <= maxAllowed;
    }
}
