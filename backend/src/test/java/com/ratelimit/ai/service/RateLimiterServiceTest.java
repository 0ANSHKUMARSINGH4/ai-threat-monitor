package com.ratelimit.ai.service;

import com.ratelimit.ai.repository.AiClassificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AiClassificationRepository aiRepo;

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(redisTemplate, aiRepo);
    }

    @Test
    void allowsRequest_whenUnderRateLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(aiRepo.findTopByIpAddressOrderByEvaluatedAtDesc(anyString()))
            .thenReturn(Optional.empty());
        when(redisTemplate.execute(
            any(DefaultRedisScript.class), anyList(), any(), any(), any(), any()))
            .thenReturn(1L);

        boolean result = rateLimiterService.isAllowed("192.168.1.1", "/api/data");
        assertTrue(result, "Request should be allowed when under rate limit");
    }

    @Test
    void blocksRequest_whenMarkedAbusive() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("ABUSIVE");

        boolean result = rateLimiterService.isAllowed("192.168.1.1", "/api/data");
        assertFalse(result, "Request should be blocked when IP is marked ABUSIVE");
    }

    @Test
    void bypassesRateLimit_forAdminEndpoints() {
        boolean result = rateLimiterService.isAllowed("192.168.1.1", "/admin/clients");
        assertTrue(result, "Admin endpoints should bypass rate limiting");
        verifyNoInteractions(redisTemplate);
    }
}
