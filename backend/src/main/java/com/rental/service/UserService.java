package com.rental.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rental.dto.OwnerRegisterDto;
import com.rental.dto.UpdateProfileDto;
import com.rental.entity.Owner;
import com.rental.enums.UserRole;
import com.rental.exception.ResourceNotFoundException;
import com.rental.repository.OwnerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;

    // ================================================================
    // REGISTRATION
    // ================================================================

    @Transactional
    public Owner registerUser(OwnerRegisterDto dto) {
        log.info("📝 Registering new user: {}", dto.getEmail());

        if (ownerRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered: " + dto.getEmail());
        }

        UserRole role = dto.getRole() != null ? dto.getRole() : UserRole.USER;

        Owner owner = Owner.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .role(role)
                .isActive(true)
                .isLocked(false)
                .joinedDate(LocalDateTime.now())
                .rating(0.0)
                .responseRate(0.0)
                .build();

        Owner savedOwner = ownerRepository.save(owner);
        log.info("✅ User registered successfully with ID: {}", savedOwner.getId());

        return savedOwner;
    }

    // ================================================================
    // FIND USERS
    // ================================================================

    public Owner getUserById(Long id) {
        log.info("🔍 Finding user by ID: {}", id);
        return ownerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public Owner getUserByEmail(String email) {
        log.info("🔍 Finding user by email: {}", email);
        return ownerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public Page<Owner> getAllUsers(Pageable pageable) {
        log.info("📋 Getting all users with pagination");
        return ownerRepository.findAllByIsActiveTrue(pageable);
    }

    public Page<Owner> getUsersByRole(String role, Pageable pageable) {
        log.info("📋 Getting users by role: {}", role);
        return ownerRepository.findByRole(role, pageable);
    }

    public List<Owner> getActiveUsers() {
        log.info("📋 Getting all active users");
        return ownerRepository.findAllByIsActiveTrue();
    }

    // ================================================================
    // UPDATE USERS
    // ================================================================

    @Transactional
    public Owner updateUser(Long id, UpdateProfileDto updateDto) {
        log.info("✏️ Updating user with ID: {}", id);

        Owner owner = ownerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (updateDto.getEmail() != null) {
            owner.setEmail(updateDto.getEmail());
        }
        if (updateDto.getName() != null) {
            owner.setName(updateDto.getName());
        }
        if (updateDto.getPhone() != null) {
            owner.setPhone(updateDto.getPhone());
        }
        if (updateDto.getAvatarUrl() != null) {
            owner.setAvatarUrl(updateDto.getAvatarUrl());
        }

        Owner updatedOwner = ownerRepository.save(owner);
        log.info("✅ User updated successfully with ID: {}", updatedOwner.getId());

        return updatedOwner;
    }

    @Transactional
    public Owner updateUserRole(Long id, UserRole role)  {
        log.info("✏️ Updating user role for ID: {} to: {}", id, role);

        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        owner.setRole(role);
        Owner updatedOwner = ownerRepository.save(owner);
        log.info("✅ User role updated successfully");

        return updatedOwner;
    }

    // ================================================================
    // SOFT DELETE / RESTORE
    // ================================================================

    @Transactional
    public void softDeleteUser(Long id) {
        log.info("🗑️ Soft deleting user with ID: {}", id);

        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        owner.setIsActive(false);
        ownerRepository.save(owner);
        log.info("✅ User soft deleted successfully");
    }

    @Transactional
    public void restoreUser(Long id) {
        log.info("🔄 Restoring user with ID: {}", id);

        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        owner.setIsActive(true);
        ownerRepository.save(owner);
        log.info("✅ User restored successfully");
    }

    // ================================================================
    // VERIFICATION
    // ================================================================

    @Transactional
    public Owner verifyUser(Long id) {
        log.info("✅ Verifying user with ID: {}", id);

        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Note: Owner doesn't have isVerified field
        // You can add it or just return
        return ownerRepository.save(owner);
    }

    // ================================================================
    // STATISTICS
    // ================================================================

    public long getTotalUserCount() {
        return ownerRepository.countByIsActiveTrue();
    }

    public long getTotalUserCountByRole(String role) {
        return ownerRepository.countByRole(role);
    }

    public long getActiveUsersCount() {
        return ownerRepository.countByIsActiveTrue();
    }

    // ================================================================
    // HELPER METHODS (For internal use)
    // ================================================================

    public Owner getOwnerEntityById(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with ID: " + id));
    }

    public boolean existsByEmail(String email) {
        return ownerRepository.existsByEmail(email);
    }
}