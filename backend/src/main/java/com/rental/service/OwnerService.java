package com.rental.service;

import com.rental.dto.OwnerLoginDto;
import com.rental.dto.OwnerRegisterDto;
import com.rental.entity.Owner;
import com.rental.exception.ResourceNotFoundException;
import com.rental.repository.OwnerRepository;
import com.rental.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private static final Logger logger = LoggerFactory.getLogger(OwnerService.class);
    private final OwnerRepository ownerRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Owner register(OwnerRegisterDto dto) {
        logger.info("Registering owner with email: {}", dto.getEmail());
        Owner owner = Owner.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .build();
        return ownerRepository.save(owner);
    }

    public String login(OwnerLoginDto dto) {
        logger.info("Login attempt for email: {}", dto.getEmail());
        Owner owner = ownerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        if (!passwordEncoder.matches(dto.getPassword(), owner.getPassword()))
            throw new RuntimeException("Invalid credentials");
        return jwtUtil.generateToken(owner.getId(), owner.getEmail());
    }

    public Owner findByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
    }
}
