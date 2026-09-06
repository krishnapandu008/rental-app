package com.rental.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rental.dto.OwnerRegisterDto;
import com.rental.dto.UpdateProfileDto;
import com.rental.dto.UserResponseDto;
import com.rental.entity.Owner;
import com.rental.enums.UserRole;
import com.rental.mapper.UserMapper;
import com.rental.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;  // ✅ Added mapper

    // ================================================================
    // REGISTRATION
    // ================================================================

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody OwnerRegisterDto registerDto) {
        log.info("📝 Registering new user: {}", registerDto.getEmail());
        Owner owner = userService.registerUser(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDtoFromOwner(owner));
    }

    // ================================================================
    // GET USERS
    // ================================================================

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("🔍 Get user by ID: {}", id);
        Owner owner = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toDtoFromOwner(owner));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
        log.info("🔍 Get user by email: {}", email);
        Owner owner = userService.getUserByEmail(email);
        return ResponseEntity.ok(userMapper.toDtoFromOwner(owner));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Owner> ownerPage = userService.getAllUsers(pageable);
        
        List<UserResponseDto> dtoList = ownerPage.getContent().stream()
                .map(userMapper::toDtoFromOwner)
                .collect(Collectors.toList());
        
        Page<UserResponseDto> response = new PageImpl<>(dtoList, pageable, ownerPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<Page<UserResponseDto>> getUsersByRole(
            @PathVariable String role,  // ✅ Changed from UserRole to String
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Owner> ownerPage = userService.getUsersByRole(role.toUpperCase(), pageable);
        
        List<UserResponseDto> dtoList = ownerPage.getContent().stream()
                .map(userMapper::toDtoFromOwner)
                .collect(Collectors.toList());
        
        Page<UserResponseDto> response = new PageImpl<>(dtoList, pageable, ownerPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponseDto>> getActiveUsers() {
        log.info("📋 Get all active users");
        List<Owner> owners = userService.getActiveUsers();
        List<UserResponseDto> response = owners.stream()
                .map(userMapper::toDtoFromOwner)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // UPDATE USERS
    // ================================================================

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileDto updateDto) {
        log.info("✏️ Update user: {}", id);
        Owner owner = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(userMapper.toDtoFromOwner(owner));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDto> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
    	 UserRole role = UserRole.valueOf(request.get("role").toUpperCase());
        log.info("✏️ Update user role: {} to {}", id, role);   
        Owner owner = userService.updateUserRole(id, role);
        return ResponseEntity.ok(userMapper.toDtoFromOwner(owner));
    }

    // ================================================================
    // SOFT DELETE / RESTORE
    // ================================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        log.info("🗑️ Soft delete user: {}", id);
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        log.info("🔄 Restore user: {}", id);
        userService.restoreUser(id);
        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // VERIFICATION
    // ================================================================

    @PatchMapping("/{id}/verify")
    public ResponseEntity<UserResponseDto> verifyUser(@PathVariable Long id) {
        log.info("✅ Verify user: {}", id);
        Owner owner = userService.verifyUser(id);
        return ResponseEntity.ok(userMapper.toDtoFromOwner(owner));
    }

    // ================================================================
    // STATISTICS
    // ================================================================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getUserStats() {
        log.info("📊 Getting user statistics");
        Map<String, Long> stats = Map.of(
            "totalUsers", userService.getTotalUserCount(),
            "activeUsers", userService.getActiveUsersCount(),
            "totalOwners", userService.getTotalUserCountByRole("OWNER"),
            "totalRenters", userService.getTotalUserCountByRole("USER")
        );
        return ResponseEntity.ok(stats);
    }
}