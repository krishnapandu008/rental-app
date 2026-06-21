package com.rental.controller;

import com.rental.dto.LoginResponseDto;
import com.rental.dto.OwnerLoginDto;
import com.rental.dto.OwnerRegisterDto;
import com.rental.dto.RegisterResponseDto;
import com.rental.entity.Owner;
import com.rental.service.OwnerService;
import com.rental.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDto register(@Valid @RequestBody OwnerRegisterDto dto) {
        Owner owner = ownerService.register(dto);
        String token = jwtUtil.generateToken(owner.getId(), owner.getEmail());
        return RegisterResponseDto.builder()
                .id(owner.getId())
                .email(owner.getEmail())
                .name(owner.getName())
                .phone(owner.getPhone())
                .token(token)
                .build();
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody OwnerLoginDto dto) {
        String token = ownerService.login(dto);
        Owner owner = ownerService.findByEmail(dto.getEmail());
        return LoginResponseDto.builder()
                .id(owner.getId())
                .email(owner.getEmail())
                .name(owner.getName())
                .phone(owner.getPhone())
                .token(token)
                .build();
    }
}
