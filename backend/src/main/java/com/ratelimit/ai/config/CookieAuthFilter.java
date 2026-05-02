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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Only protect /admin/** routes
        if (path.startsWith("/admin") && !path.startsWith("/admin/health")) {
            boolean isAuthenticated = false;
            
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("SESSION_TOKEN".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                        isAuthenticated = true;
                        // Set up Spring Security Context for this request
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin", null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        break;
                    }
                }
            }
            
            if (!isAuthenticated) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Unauthorized - Missing or Invalid HttpOnly Cookie\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
