package com.rental.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rental.dto.TokenRefreshResponseDto;
import com.rental.entity.Owner;
import com.rental.entity.RefreshToken;
import com.rental.exception.UnauthorizedException;
import com.rental.service.OwnerService;
import com.rental.service.RefreshTokenService;
import com.rental.util.CookieUtil;
import com.rental.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final OwnerService ownerService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @PostMapping("/refresh")
    public TokenRefreshResponseDto refresh(HttpServletRequest request,
                                           HttpServletResponse response) {
        // Extract refresh token from cookie
        String refreshToken = getCookieValue(request, "refreshToken");
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token missing");
        }
        RefreshToken stored = refreshTokenService.verifyAndGet(refreshToken);
        Owner owner = ownerService.findById(stored.getOwnerId());

        String newAccessToken = jwtUtil.generateToken(owner.getId(), owner.getEmail(), owner.getRole().name());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(owner.getId());

        // Set new cookies
        cookieUtil.setTokenCookie(response, newAccessToken);
        cookieUtil.setRefreshTokenCookie(response, newRefreshToken.getToken());

        // Return only success (or empty)
        return TokenRefreshResponseDto.builder()
                .accessToken(null)  // Not needed; cookie will be sent
                .refreshToken(null)
                .tokenType("Bearer")
                .build();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Optional: revoke the refresh token from database
        String refreshToken = getCookieValue(request, "refreshToken");
        if (refreshToken != null) {
            refreshTokenService.revokeByToken(refreshToken);
        }
        cookieUtil.clearTokenCookies(response);
    }
    
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}