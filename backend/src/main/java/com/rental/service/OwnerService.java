package com.rental.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rental.dto.ChangePasswordDto;
import com.rental.dto.OwnerLoginDto;
import com.rental.dto.OwnerProfileDto;
import com.rental.dto.OwnerRegisterDto;
import com.rental.dto.OwnerSummaryDto;
import com.rental.dto.UpdateProfileDto;
import com.rental.entity.Owner;
import com.rental.enums.UserRole;   // ✅ Import enum
import com.rental.exception.ResourceNotFoundException;
import com.rental.exception.UnauthorizedException;
import com.rental.mapper.OwnerMapper;
import com.rental.repository.OwnerRepository;
import com.rental.repository.PropertyRepository;
import com.rental.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OwnerService {
    
    private final OwnerRepository ownerRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LocalStorageService storageService;
    private final OwnerMapper ownerMapper;
    private final PropertyRepository propertyRepository;

    // ================================================================
    // REGISTRATION
    // ================================================================

    @Transactional
    public Owner register(OwnerRegisterDto dto) {
        log.info("📝 Registering new owner: {}", dto.getEmail());
        
        // ✅ Use enum directly from DTO, default to USER if null
        UserRole role = dto.getRole() != null ? dto.getRole() : UserRole.USER;

        Owner owner = Owner.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .role(role)  // ✅ Now enum
                .isActive(true)
                .isLocked(false)
                .rating(0.0)
                .responseRate(0.0)
                .joinedDate(LocalDateTime.now())
                .build();
        
        Owner savedOwner = ownerRepository.save(owner);
        log.info("✅ Owner registered successfully with ID: {}", savedOwner.getId());
        
        return savedOwner;
    }

    // ================================================================
    // AUTHENTICATION
    // ================================================================

    public String login(OwnerLoginDto dto) {
        log.info("🔐 Login attempt for: {}", dto.getEmail());
        
        Owner owner = ownerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        // ✅ P0-2: Check if the account is locked
        if (owner.isLocked()) {
            log.warn("🚫 Login attempt for locked account: {}", dto.getEmail());
            throw new UnauthorizedException("Your account has been locked. Please contact support.");
        }
        
        if (!passwordEncoder.matches(dto.getPassword(), owner.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        owner.setLastLoginAt(LocalDateTime.now());
        ownerRepository.save(owner);
        
        // ✅ role is enum, but JwtUtil expects String – we pass enum name
        return jwtUtil.generateToken(owner.getId(), owner.getEmail(), owner.getRole().name());
    }

    // ================================================================
    // FIND OWNERS
    // ================================================================

    public Owner findByEmail(String email) {
        log.info("🔍 Finding owner by email: {}", email);
        return ownerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with email: " + email));
    }

    public Owner findById(Long id) {
        log.info("🔍 Finding owner by ID: {}", id);
        return ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with ID: " + id));
    }
    public OwnerProfileDto getOwnerProfile(Long id) {
        Owner owner = findById(id);
        OwnerProfileDto dto = ownerMapper.toProfileDto(owner);
        dto.setListingCount((int) propertyRepository.countActivePropertiesByOwnerId(id));
        return dto;
    }

    public List<OwnerSummaryDto> getAllOwners() {
        return ownerRepository.findAll().stream()
            .map(owner -> {
                OwnerSummaryDto dto = ownerMapper.toDto(owner);
                dto.setListingCount((int) propertyRepository.countActivePropertiesByOwnerId(owner.getId()));
                return dto;
            })
            .collect(Collectors.toList());
    }

    // ================================================================
    // ADMIN/SUPER_ADMIN METHODS
    // ================================================================

    @Transactional
    public Owner createUser(OwnerRegisterDto dto) {
        log.info("👤 Creating new user: {}", dto.getEmail());
        return register(dto);
    }

    @Transactional
    public Owner updateUser(Long id, UpdateProfileDto dto) {
        log.info("✏️ Updating user with ID: {}", id);
        
        Owner owner = findById(id);
        if (dto.getEmail() != null) {
            owner.setEmail(dto.getEmail());
        }
        if (dto.getName() != null) {
            owner.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            owner.setPhone(dto.getPhone());
        }
        
        Owner updatedOwner = ownerRepository.save(owner);
        log.info("✅ User updated successfully with ID: {}", updatedOwner.getId());
        
        return updatedOwner;
    }

    @Transactional
    public Owner toggleActive(Long id) {
        log.info("🔄 Toggling active status for user ID: {}", id);
        
        Owner owner = findById(id);
        owner.setIsActive(!owner.isActive());
        Owner updatedOwner = ownerRepository.save(owner);
        
        log.info("✅ User active status toggled to: {}", updatedOwner.isActive());
        return updatedOwner;
    }

    // ✅ Updated to accept UserRole enum
    @Transactional
    public Owner updateRole(Long id, UserRole newRole) {
        log.info("✏️ Updating role for user ID: {} to: {}", id, newRole);
        
        Owner owner = findById(id);
        owner.setRole(newRole);   // ✅ Now enum
        Owner updatedOwner = ownerRepository.save(owner);
        log.info("✅ User role updated successfully to: {}", updatedOwner.getRole());
        
        return updatedOwner;
    }

    // ================================================================
    // PROFILE MANAGEMENT
    // ================================================================

    @Transactional
    public Owner updateProfile(Long id, UpdateProfileDto dto) {
        log.info("✏️ Updating profile for user ID: {}", id);
        
        Owner owner = findById(id);
        if (dto.getEmail() != null) {
            owner.setEmail(dto.getEmail());
        }
        if (dto.getName() != null) {
            owner.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            owner.setPhone(dto.getPhone());
        }
        if (dto.getAvatarUrl() != null) {
            owner.setAvatarUrl(dto.getAvatarUrl());
        }
        
        Owner updatedOwner = ownerRepository.save(owner);
        log.info("✅ Profile updated successfully for ID: {}", updatedOwner.getId());
        
        return updatedOwner;
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordDto dto) {
        log.info("🔑 Changing password for user ID: {}", id);
        
        Owner owner = findById(id);
        if (!passwordEncoder.matches(dto.getOldPassword(), owner.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        owner.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        ownerRepository.save(owner);
        
        log.info("✅ Password changed successfully for user ID: {}", id);
    }

    @Transactional
    public String uploadAvatar(Long id, MultipartFile file) throws IOException {
        log.info("📷 Uploading avatar for user ID: {}", id);
        
        Owner owner = findById(id);
        if (owner.getAvatarUrl() != null) {
            storageService.deleteFile(owner.getAvatarUrl());
        }
        String avatarUrl = storageService.saveFile(file);
        owner.setAvatarUrl(avatarUrl);
        ownerRepository.save(owner);
        
        log.info("✅ Avatar uploaded successfully for user ID: {}", id);
        return avatarUrl;
    }
}