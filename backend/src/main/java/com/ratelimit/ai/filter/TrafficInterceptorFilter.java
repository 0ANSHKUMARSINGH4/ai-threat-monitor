package com.ratelimit.ai.filter;

import com.ratelimit.ai.domain.RequestLog;
import com.ratelimit.ai.repository.RequestLogRepository;
import com.ratelimit.ai.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Component
public class TrafficInterceptorFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final RequestLogRepository requestLogRepository;

    public TrafficInterceptorFilter(RateLimiterService rateLimiterService, 
                                    RequestLogRepository requestLogRepository) {
        this.rateLimiterService = rateLimiterService;
        this.requestLogRepository = requestLogRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper cachedReq = new ContentCachingRequestWrapper(request);
        String ip = request.getRemoteAddr();

        if (cachedReq.getRequestURI().startsWith("/admin") || 
            cachedReq.getRequestURI().startsWith("/health") || 
            cachedReq.getRequestURI().startsWith("/error")) {
            filterChain.doFilter(cachedReq, response);
            return;
        }

        if (!rateLimiterService.isAllowed(ip, cachedReq.getRequestURI())) {
            response.setStatus(429);
            response.getWriter().write("{\"error\": \"Too Many Requests\"}");
            response.setContentType("application/json");
            return;
        }

        filterChain.doFilter(cachedReq, response);

        byte[] contentAsByteArray = cachedReq.getContentAsByteArray();
        String payloadHash = hash(contentAsByteArray);

        // Safely log the request - do not crash the response if logging fails
        try {
            RequestLog logEntry = new RequestLog(
                    ip,
                    cachedReq.getRequestURI() + (cachedReq.getQueryString() != null ? "?" + cachedReq.getQueryString() : ""),
                    payloadHash,
                    request.getHeader("User-Agent"),
                    LocalDateTime.now()
            );
            requestLogRepository.save(logEntry);
        } catch (Exception e) {
            // Log the error but keep the app running
            System.err.println("Failed to log request to database: " + e.getMessage());
        }
    }

    private String hash(byte[] payload) {
        if (payload == null || payload.length == 0) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(payload);
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "hash_error";
        }
    }
}
