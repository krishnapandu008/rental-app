package com.rental.controller;

import com.rental.dto.LoginResponseDto;
import com.rental.dto.OwnerLoginDto;
import com.rental.dto.OwnerRegisterDto;
import com.rental.dto.OwnerSummaryDto;
import com.rental.dto.RegisterResponseDto;
import com.rental.entity.Owner;
import com.rental.entity.RefreshToken;
import com.rental.service.OwnerService;
import com.rental.service.RefreshTokenService;
import com.rental.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
                .build();
    }

    // Admin-only: list every owner account (SecurityConfig restricts this to ROLE_ADMIN)
    @GetMapping
    public List<OwnerSummaryDto> getAllOwners() {
        return ownerService.getAllOwners();
    }
}