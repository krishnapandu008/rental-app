package com.rental.config;

import java.util.Arrays;   // added
import java.util.List;

import org.springframework.beans.factory.annotation.Value;   // added
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.rental.filter.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Read CORS allowed origins from application.yaml (with fallback)
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);   // ✅ NEW: Cache preflight for 1 hour
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));  // changed
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);   // ✅ NEW: Cache preflight for 1 hour
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));  // changed
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Custom matcher to allow all GET requests that are NOT API, NOT static resources,
     * and NOT favicon. This enables React Router to serve the SPA (index.html)
     * for any frontend route without requiring authentication.
     */
    private RequestMatcher spaRequestMatcher() {
        return request -> {
            String method = request.getMethod();
            String path = request.getServletPath();
            return "GET".equalsIgnoreCase(method)
                    && !path.startsWith("/api/")
                    && !path.startsWith("/images/")
                    && !path.startsWith("/static/")
                    && !path.startsWith("/assets/")
                    && !path.startsWith("/favicon.ico");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(corsFilter(), CorsFilter.class)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // === PUBLIC ENDPOINTS ===
                // 1. Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 2. Authentication & Registration
                .requestMatchers("/api/owners/register", "/api/owners/login").permitAll()
                .requestMatchers("/api/auth/refresh", "/api/auth/logout").permitAll()

                // 3. Public property listings (read-only)
                .requestMatchers(HttpMethod.GET, "/api/properties").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/properties/{id}").permitAll()

                // 4. AI health check (GET only)
                .requestMatchers(HttpMethod.GET, "/api/ai/health").permitAll()

                // 5. Location suggestions (used by frontend search bar, no auth required)
                .requestMatchers(HttpMethod.GET, "/api/properties/locations/suggest").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/locations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/locations/active").permitAll()

                // 6. Static resources (images, assets)
                .requestMatchers("/images/**", "/static/**", "/assets/**", "/favicon.ico").permitAll()

                // 7. SPA Forwarding: any other GET (not API, not static) is permitted
                .requestMatchers(spaRequestMatcher()).permitAll()

                // === ADMIN ENDPOINTS ===
                .requestMatchers(HttpMethod.GET, "/api/properties/admin/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/owners").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // === AUTHENTICATED ENDPOINTS (all other requests) ===
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}