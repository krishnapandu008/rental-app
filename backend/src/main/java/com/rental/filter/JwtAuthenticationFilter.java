package com.rental.filter;

import com.rental.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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
        logger.info("Filter invoked for path: " + path);

        // 1. Skip OPTIONS preflight requests (no JWT expected)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            logger.info("Skipping OPTIONS preflight");
            chain.doFilter(request, response);
            return;
        }

        // 2. Skip public endpoints
        if (path.startsWith("/api/owners/register") ||
            path.startsWith("/api/owners/login") ||
            (path.equals("/api/properties") && "GET".equalsIgnoreCase(request.getMethod()))) {
            logger.info("Skipping token check for public path: " + path);
            chain.doFilter(request, response);
            return;
        }

        // 3. Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization header: " + authHeader);

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.info("Extracted token: " + token);
        } else {
            logger.warn("No Bearer token found in Authorization header");
        }

        // 4. Validate token and set authentication
        if (token != null && jwtUtil.isTokenValid(token)) {
            try {
                Long ownerId = jwtUtil.getOwnerIdFromToken(token);
                request.setAttribute("ownerId", ownerId);
                logger.info("Set ownerId: " + ownerId);

                String role = jwtUtil.getRoleFromToken(token);
                if (role == null || role.isBlank()) {
                    role = "OWNER";
                }
                request.setAttribute("role", role);
                logger.info("Set role: " + role);

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // 🔥 Register the user with Spring Security
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        ownerId.toString(), null, authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                logger.error("Failed to parse JWT token", e);
                // Optionally clear the context if token is invalid
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.warn("Token is null or invalid");
            // Ensure no stale authentication remains
            SecurityContextHolder.clearContext();
        }

        // 5. Continue the filter chain
        chain.doFilter(request, response);
    }
}