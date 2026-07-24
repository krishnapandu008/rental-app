package com.rental.service;

import com.rental.dto.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private static final Logger logger = LoggerFactory.getLogger(OwnerService.class);
    private final OwnerRepository ownerRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ---------- Existing methods ----------
    public Owner register(OwnerRegisterDto dto) {
        Owner owner = Owner.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .role("USER")   // default role
                .isActive(true)
                .isLocked(false)
                .build();
        return ownerRepository.save(owner);
    }

    public String login(OwnerLoginDto dto) {
        Owner owner = ownerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(dto.getPassword(), owner.getPassword()))
            throw new UnauthorizedException("Invalid credentials");
        // Update last login time
        owner.setLastLoginAt(LocalDateTime.now());
        ownerRepository.save(owner);
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

    // ---------- NEW: Admin/SUPER_ADMIN methods ----------
    public Owner createUser(CreateUserDto dto) {
        String role = dto.getRole() != null ? dto.getRole() : "USER";
        Owner owner = Owner.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .role(role)
                .isActive(true)
                .isLocked(false)
                .build();
        return ownerRepository.save(owner);
    }

    public Owner updateUser(Long id, UpdateUserDto dto) {
        Owner owner = findById(id);
        owner.setEmail(dto.getEmail());
        owner.setName(dto.getName());
        owner.setPhone(dto.getPhone());
        return ownerRepository.save(owner);
    }

    public Owner toggleActive(Long id) {
        Owner owner = findById(id);
        owner.setActive(!owner.isActive());
        return ownerRepository.save(owner);
    }

    public Owner updateRole(Long id, String newRole) {
        Owner owner = findById(id);
        // Prevent demoting SUPER_ADMIN if you are not SUPER_ADMIN – handled in controller
        owner.setRole(newRole);
        return ownerRepository.save(owner);
    }
}