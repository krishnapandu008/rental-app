package com.rental.filter;

import com.rental.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain chain)
        throws ServletException, IOException {

    String path = request.getRequestURI();
    logger.info("Filter invoked for path: " + path);   // 👈 ADD THIS

    // Skip token check for public endpoints
   
    if (path.startsWith("/api/owners/register") ||
    path.startsWith("/api/owners/login") ||
    (path.equals("/api/properties") && "GET".equalsIgnoreCase(request.getMethod()))) {
        logger.info("Skipping token check for public path: " + path);
    chain.doFilter(request, response);
    return;
}

    String authHeader = request.getHeader("Authorization");
    logger.info("Authorization header: " + authHeader);   // 👈 ADD THIS

    String token = null;
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        token = authHeader.substring(7);
        logger.info("Extracted token: " + token);
    } else {
        logger.warn("No Bearer token found in Authorization header");
    }

    if (token != null && jwtUtil.isTokenValid(token)) {
        try {
            Long ownerId = jwtUtil.getOwnerIdFromToken(token);
            request.setAttribute("ownerId", ownerId);
            logger.info("Set ownerId: " + ownerId);
        } catch (Exception e) {
            logger.error("Failed to parse JWT token", e);
        }
    } else {
        logger.warn("Token is null or invalid");
    }

    chain.doFilter(request, response);
}
}