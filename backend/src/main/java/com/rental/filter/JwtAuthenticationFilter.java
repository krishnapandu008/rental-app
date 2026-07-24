package com.rental.filter;

import com.rental.security.OwnerPrincipal;
import com.rental.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("Filter invoked for path: {}", path);

        // 1. Skip OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            logger.info("Skipping OPTIONS preflight");
            chain.doFilter(request, response);
            return;
        }

        // 2. Skip public endpoints (no token required)
        if (path.startsWith("/api/owners/register") ||
            path.startsWith("/api/owners/login") ||
            (path.equals("/api/properties") && "GET".equalsIgnoreCase(request.getMethod()))) {
            logger.info("Skipping token check for public path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        // 3. Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authHeader);

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.info("Extracted token: {}", token);
        } else {
            logger.warn("No Bearer token found in Authorization header");
        }

        // 4. Validate token and set authentication
        if (token != null && jwtUtil.isTokenValid(token)) {
            try {
                Long ownerId = jwtUtil.getOwnerIdFromToken(token);
                String email = jwtUtil.getEmailFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                logger.info("Extracted role from token: {}", role);

                // Normalize role (default to "OWNER" if missing)
                if (role == null || role.isBlank()) {
                    role = "OWNER";
                }

                // ✅ Create authorities ONLY ONCE after normalization
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                logger.info("Authorities: {}", authorities);

                // Create principal object
                OwnerPrincipal principal = new OwnerPrincipal(ownerId, email, role);

                // Set authentication in Spring Security context
                Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // (Optional) Keep request attributes for backward compatibility
                request.setAttribute("ownerId", ownerId);
                request.setAttribute("role", role);

                logger.info("Authenticated ownerId: {}, role: {}", ownerId, role);

            } catch (Exception e) {
                logger.error("Failed to parse JWT token", e);
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.warn("Token is null or invalid");
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}