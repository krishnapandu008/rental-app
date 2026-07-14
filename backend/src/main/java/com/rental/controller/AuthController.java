package com.rental.controller;

import com.rental.dto.RefreshTokenRequestDto;
import com.rental.dto.TokenRefreshResponseDto;
import com.rental.entity.Owner;
import com.rental.entity.RefreshToken;
import com.rental.service.OwnerService;
import com.rental.service.RefreshTokenService;
import com.rental.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final OwnerService ownerService;
    private final JwtUtil jwtUtil;

    /**
     * Exchanges a valid, unexpired refresh token for a brand new access token
     * (and a rotated refresh token). Called by clients when their access
     * token has expired, instead of forcing the user to log in again.
     */
    @PostMapping("/refresh")
    public TokenRefreshResponseDto refresh(@Valid @RequestBody RefreshTokenRequestDto dto) {
        RefreshToken stored = refreshTokenService.verifyAndGet(dto.getRefreshToken());
        Owner owner = ownerService.findById(stored.getOwnerId());

        String newAccessToken = jwtUtil.generateToken(owner.getId(), owner.getEmail(), owner.getRole());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(owner.getId());

        return TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    /**
     * Revokes a refresh token so it can no longer be used to mint new access
     * tokens. The access token itself keeps working until it naturally
     * expires (it's stateless), but the session can't be silently renewed.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequestDto dto) {
        refreshTokenService.revokeByToken(dto.getRefreshToken());
    }
}