package com.rental.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rental.dto.ChangePasswordDto;
import com.rental.dto.LoginResponseDto;
import com.rental.dto.OwnerLoginDto;
import com.rental.dto.OwnerProfileDto;
import com.rental.dto.OwnerRegisterDto;
import com.rental.dto.OwnerSummaryDto;
import com.rental.dto.RegisterResponseDto;
import com.rental.dto.UpdateProfileDto;
import com.rental.entity.Owner;
import com.rental.entity.RefreshToken;
import com.rental.mapper.OwnerMapper;
import com.rental.security.OwnerPrincipal;
import com.rental.service.OwnerService;
import com.rental.service.RefreshTokenService;
import com.rental.util.CookieUtil;
import com.rental.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final OwnerMapper ownerMapper;
    private final CookieUtil cookieUtil;

    // ---- Public endpoints (no auth) ----
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDto register(@Valid @RequestBody OwnerRegisterDto dto) {
        Owner owner = ownerService.register(dto);
        String token = jwtUtil.generateToken(owner.getId(), owner.getEmail(), owner.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(owner.getId());
        return ownerMapper.toRegisterResponseDto(owner, token, refreshToken.getToken());
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody OwnerLoginDto dto,
                                  HttpServletResponse response) {
        String token = ownerService.login(dto);
        Owner owner = ownerService.findByEmail(dto.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(owner.getId());

        // Set HttpOnly cookies
        cookieUtil.setTokenCookie(response, token);
        cookieUtil.setRefreshTokenCookie(response, refreshToken.getToken());

        // Return owner data without tokens (or keep them for backward compatibility)
        return ownerMapper.toLoginResponseDto(owner, null, null);
    }

    // ---- Admin-only ----
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<OwnerSummaryDto> getAllOwners() {
        return ownerService.getAllOwners();
    }

    // ---- Authenticated user endpoints ----
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public OwnerProfileDto getProfile(@AuthenticationPrincipal OwnerPrincipal principal) {
        return ownerService.getOwnerProfile(principal.getId());
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public OwnerProfileDto updateProfile(@Valid @RequestBody UpdateProfileDto dto,
                                         @AuthenticationPrincipal OwnerPrincipal principal) {
        Owner updated = ownerService.updateProfile(principal.getId(), dto);
        return ownerMapper.toProfileDto(updated);
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDto dto,
                                               @AuthenticationPrincipal OwnerPrincipal principal) {
        ownerService.changePassword(principal.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                               @AuthenticationPrincipal OwnerPrincipal principal) throws IOException {
        String avatarUrl = ownerService.uploadAvatar(principal.getId(), file);
        return ResponseEntity.ok(avatarUrl);
    }
}