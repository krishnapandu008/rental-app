package com.rental.service;

import com.rental.dto.OwnerLoginDto;
import com.rental.dto.OwnerRegisterDto;
import com.rental.dto.OwnerSummaryDto;
import com.rental.entity.Owner;
import com.rental.exception.ResourceNotFoundException;
import com.rental.exception.UnauthorizedException;
import com.rental.repository.OwnerRepository;
import com.rental.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private static final Logger logger = LoggerFactory.getLogger(OwnerService.class);
    private final OwnerRepository ownerRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Owner register(OwnerRegisterDto dto) {
        Owner owner = Owner.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .role("OWNER")   // ← set default role
                .build();
        return ownerRepository.save(owner);
    }

    public String login(OwnerLoginDto dto) {
        Owner owner = ownerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(dto.getPassword(), owner.getPassword()))
            throw new UnauthorizedException("Invalid credentials");
        return jwtUtil.generateToken(owner.getId(), owner.getEmail(), owner.getRole());
    }

    public Owner findByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
    }

    public Owner findById(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
    }

    public List<OwnerSummaryDto> getAllOwners() {
        return ownerRepository.findAll().stream()
                .map(o -> OwnerSummaryDto.builder()
                        .id(o.getId())
                        .email(o.getEmail())
                        .name(o.getName())
                        .phone(o.getPhone())
                        .role(o.getRole())
                        .build())
                .collect(Collectors.toList());
    }
}