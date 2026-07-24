package com.rental.controller;

import com.rental.dto.*;
import com.rental.entity.Owner;
import com.rental.exception.ForbiddenException;
import com.rental.exception.ResourceNotFoundException;
import com.rental.repository.OwnerRepository;
import com.rental.security.OwnerPrincipal;
import com.rental.service.AuditLogService;
import com.rental.service.OwnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
// ✅ @CrossOrigin REMOVED – global CorsFilter handles it
@RequiredArgsConstructor
public class AdminController {

    private final OwnerRepository ownerRepository;
    private final OwnerService ownerService;
    private final AuditLogService auditLogService;

    // ---------- All admins can view users ----------
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<OwnerSummaryDto> getAllUsers() {
        return ownerRepository.findAll().stream()
                .map(o -> new OwnerSummaryDto(o.getId(), o.getEmail(), o.getName(), o.getPhone(), o.getRole()))
                .collect(Collectors.toList());
    }

    // ---------- View user details ----------
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public OwnerDetailsDto getUserDetails(@PathVariable Long id) {
        Owner owner = ownerService.findById(id);
        return new OwnerDetailsDto(
                owner.getId(),
                owner.getEmail(),
                owner.getName(),
                owner.getPhone(),
                owner.getRole(),
                owner.isActive(),
                owner.isLocked(),
                owner.getCreatedAt(),
                owner.getUpdatedAt(),
                owner.getLastLoginAt()
        );
    }

    // ---------- Create user (SUPER_ADMIN only) ----------
    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<OwnerSummaryDto> createUser(@Valid @RequestBody CreateUserDto dto,
                                                      @AuthenticationPrincipal OwnerPrincipal principal,
                                                      HttpServletRequest request) {
        Owner created = ownerService.createUser(dto);
        auditLogService.log(
                ownerService.findById(principal.getId()),
                "CREATE_USER",
                "Created user: " + created.getEmail(),
                request
        );
        return ResponseEntity.ok(new OwnerSummaryDto(
                created.getId(), created.getEmail(), created.getName(), created.getPhone(), created.getRole()
        ));
    }

    // ---------- Update user (SUPER_ADMIN only) ----------
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<OwnerSummaryDto> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateUserDto dto,
                                                      @AuthenticationPrincipal OwnerPrincipal principal,
                                                      HttpServletRequest request) {
        Owner updated = ownerService.updateUser(id, dto);
        auditLogService.log(
                ownerService.findById(principal.getId()),
                "UPDATE_USER",
                "Updated user: " + updated.getEmail(),
                request
        );
        return ResponseEntity.ok(new OwnerSummaryDto(
                updated.getId(), updated.getEmail(), updated.getName(), updated.getPhone(), updated.getRole()
        ));
    }

    // ---------- Update role (SUPER_ADMIN only) ----------
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<OwnerSummaryDto> updateUserRole(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateRoleRequestDto dto,
                                                          @AuthenticationPrincipal OwnerPrincipal principal,
                                                          HttpServletRequest request) {
        Owner owner = ownerService.findById(id);
        String oldRole = owner.getRole();
        owner = ownerService.updateRole(id, dto.getRole());
        auditLogService.log(
                ownerService.findById(principal.getId()),
                "UPDATE_ROLE",
                "Changed role of " + owner.getEmail() + " from " + oldRole + " to " + dto.getRole(),
                request
        );
        return ResponseEntity.ok(new OwnerSummaryDto(
                owner.getId(), owner.getEmail(), owner.getName(), owner.getPhone(), owner.getRole()
        ));
    }

    // ---------- Toggle active (soft delete) – Admins and SUPER_ADMIN ----------
    @PatchMapping("/users/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<OwnerSummaryDto> toggleActive(@PathVariable Long id,
                                                        @AuthenticationPrincipal OwnerPrincipal principal,
                                                        HttpServletRequest request) {
        Owner owner = ownerService.toggleActive(id);
        String action = owner.isActive() ? "RESTORE_USER" : "DEACTIVATE_USER";
        auditLogService.log(
                ownerService.findById(principal.getId()),
                action,
                owner.isActive() ? "Restored user: " + owner.getEmail() : "Deactivated user: " + owner.getEmail(),
                request
        );
        return ResponseEntity.ok(new OwnerSummaryDto(
                owner.getId(), owner.getEmail(), owner.getName(), owner.getPhone(), owner.getRole()
        ));
    }

    // ---------- Delete user (hard delete – SUPER_ADMIN only) ----------
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal OwnerPrincipal principal,
                                           HttpServletRequest request) {
        Owner owner = ownerService.findById(id);
        ownerRepository.delete(owner);
        auditLogService.log(
                ownerService.findById(principal.getId()),
                "DELETE_USER",
                "Deleted user: " + owner.getEmail(),
                request
        );
        return ResponseEntity.noContent().build();
    }

    // ---------- View audit logs (SUPER_ADMIN only) ----------
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<AuditLogDto> getAuditLogs() {
        return auditLogService.getAllLogs();
    }
}