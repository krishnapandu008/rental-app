package com.rental.config;

import com.rental.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // Safe hashing baseline
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 🚀 CRITICAL FIX: Allow all pre-flight OPTIONS handshakes globally for mobile networking
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public endpoints (no token required)
                .requestMatchers("/api/owners/register", "/api/owners/login").permitAll()
                .requestMatchers("/api/auth/refresh", "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/properties").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/properties/{id}").permitAll()

                // Admin-only endpoints (must come before the generic authenticated rules below)
                .requestMatchers(HttpMethod.GET, "/api/properties/admin/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/owners").hasRole("ADMIN")

                // Owner-scoped lookup: any authenticated user may call this;
                // the controller itself enforces "own data or admin"
                .requestMatchers(HttpMethod.GET, "/api/properties/owner/{ownerId}").authenticated()

                // Protected endpoints (valid JWT required)
                .requestMatchers(HttpMethod.POST, "/api/properties", "/api/properties/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/properties/{id}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/properties/{id}").authenticated()

                // All other requests (static files, images, React routes) are public
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Explicitly align cross-origin network constraints to allow wildcard resolution profiles
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization")); // Helps client read tokens if needed
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}