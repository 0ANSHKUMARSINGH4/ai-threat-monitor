package com.ratelimit.ai.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class CookieAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip filtering for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String path = request.getRequestURI();
            
            if (path.startsWith("/admin") && !path.startsWith("/admin/health")) {
                String authHeader = request.getHeader("Authorization");
                boolean isAuthenticated = false;

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (token != null && !token.isEmpty()) {
                        isAuthenticated = true;
                        UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                "admin", null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }

                // Also still accept cookie for backward compatibility
                if (!isAuthenticated && request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if ("SESSION_TOKEN".equals(cookie.getName()) 
                                && cookie.getValue() != null 
                                && !cookie.getValue().isEmpty()) {
                            isAuthenticated = true;
                            UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                    "admin", null, Collections.emptyList());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            break;
                        }
                    }
                }
                
                if (!isAuthenticated) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"error\": \"Unauthorized\"}");
                    return;
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            response.setStatus(500);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"filterError\":\"" + e.getClass().getName() + 
                "\",\"message\":\"" + 
                (e.getMessage() != null ? 
                    e.getMessage().replace("\"", "'") : "null") + 
                "\",\"cause\":\"" + 
                (e.getCause() != null ? 
                    e.getCause().getClass().getName() : "null") + 
                "\"}");
        }
    }
}
