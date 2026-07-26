package com.rental.controller;

import com.rental.dto.*;
import com.rental.entity.Owner;
import com.rental.entity.RefreshToken;
import com.rental.security.OwnerPrincipal;
import com.rental.service.OwnerService;
import com.rental.service.RefreshTokenService;
import com.rental.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDto register(@Valid @RequestBody OwnerRegisterDto dto) {
        Owner owner = ownerService.register(dto);
        String token = jwtUtil.generateToken(owner.getId(), owner.getEmail(), owner.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(owner.getId());
        return RegisterResponseDto.builder()
                .id(owner.getId())
                .email(owner.getEmail())
                .name(owner.getName())
                .phone(owner.getPhone())
                .token(token)
                .refreshToken(refreshToken.getToken())
                .role(owner.getRole())
                .avatarUrl(owner.getAvatarUrl())   // ✅ add this
                .build();
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody OwnerLoginDto dto) {
        String token = ownerService.login(dto);
        Owner owner = ownerService.findByEmail(dto.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(owner.getId());
        return LoginResponseDto.builder()
                .id(owner.getId())
                .email(owner.getEmail())
                .name(owner.getName())
                .phone(owner.getPhone())
                .token(token)
                .refreshToken(refreshToken.getToken())
                .role(owner.getRole())
                .avatarUrl(owner.getAvatarUrl())   // ✅ add this
                .build();
    }

    // Admin-only: list every owner account
    @GetMapping
    public List<OwnerSummaryDto> getAllOwners() {
        return ownerService.getAllOwners();
    }

    // ---------- Profile endpoints ----------
    @GetMapping("/profile")
    public OwnerProfileDto getProfile(@AuthenticationPrincipal OwnerPrincipal principal) {
        Owner owner = ownerService.findById(principal.getId());
        return OwnerProfileDto.builder()
                .id(owner.getId())
                .email(owner.getEmail())
                .name(owner.getName())
                .phone(owner.getPhone())
                .role(owner.getRole())
                .avatarUrl(owner.getAvatarUrl())
                .createdAt(owner.getCreatedAt())
                .build();
    }

    @PutMapping("/profile")
    public OwnerProfileDto updateProfile(@Valid @RequestBody UpdateProfileDto dto,
                                         @AuthenticationPrincipal OwnerPrincipal principal) {
        Owner updated = ownerService.updateProfile(principal.getId(), dto);
        return OwnerProfileDto.builder()
                .id(updated.getId())
                .email(updated.getEmail())
                .name(updated.getName())
                .phone(updated.getPhone())
                .role(updated.getRole())
                .avatarUrl(updated.getAvatarUrl())
                .createdAt(updated.getCreatedAt())
                .build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDto dto,
                                               @AuthenticationPrincipal OwnerPrincipal principal) {
        ownerService.changePassword(principal.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                               @AuthenticationPrincipal OwnerPrincipal principal) throws IOException {
        String avatarUrl = ownerService.uploadAvatar(principal.getId(), file);
        return ResponseEntity.ok(avatarUrl);
    }
}