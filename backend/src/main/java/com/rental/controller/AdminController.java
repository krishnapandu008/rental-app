package com.rental.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.rental.dto.AuditLogDto;
import com.rental.dto.OwnerDetailsDto;
import com.rental.dto.OwnerRegisterDto;
import com.rental.dto.OwnerSummaryDto;
import com.rental.dto.UpdateProfileDto;
import com.rental.dto.UpdateRoleRequestDto;
import com.rental.entity.Owner;
import com.rental.mapper.OwnerMapper;
import com.rental.repository.OwnerRepository;
import com.rental.security.OwnerPrincipal;
import com.rental.service.AuditLogService;
import com.rental.service.OwnerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OwnerRepository ownerRepository;
    private final OwnerService ownerService;
    private final AuditLogService auditLogService;
    private final OwnerMapper ownerMapper; // ✅ Added mapper

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<OwnerSummaryDto> getAllUsers() {
        return ownerRepository.findAll().stream()
                .map(ownerMapper::toDto) // ✅ Using mapper
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public OwnerDetailsDto getUserDetails(@PathVariable Long id) {
        Owner owner = ownerService.findById(id);
        return ownerMapper.toDetailsDto(owner); // ✅ Using mapper
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<OwnerSummaryDto> createUser(@Valid @RequestBody OwnerRegisterDto dto,
                                                      @AuthenticationPrincipal OwnerPrincipal principal,
                                                      HttpServletRequest request) {
        Owner created = ownerService.register(dto);
        auditLogService.log(
                ownerService.findById(principal.getId()),
                "CREATE_USER",
                "Created user: " + created.getEmail(),
                request
        );
        return ResponseEntity.ok(ownerMapper.toDto(created)); // ✅ Using mapper
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<OwnerSummaryDto> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateProfileDto dto,
                                                      @AuthenticationPrincipal OwnerPrincipal principal,
                                                      HttpServletRequest request) {
        Owner updated = ownerService.updateProfile(id, dto);
        auditLogService.log(
                ownerService.findById(principal.getId()),
                "UPDATE_USER",
                "Updated user: " + updated.getEmail(),
                request
        );
        return ResponseEntity.ok(ownerMapper.toDto(updated)); // ✅ Using mapper
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<OwnerSummaryDto> updateUserRole(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateRoleRequestDto dto,
                                                          @AuthenticationPrincipal OwnerPrincipal principal,
                                                          HttpServletRequest request) {
        Owner owner = ownerService.findById(id);
        String oldRole = owner.getRole().name();
        owner = ownerService.updateRole(id, dto.getRole());
        auditLogService.log(
                ownerService.findById(principal.getId()),
                "UPDATE_ROLE",
                "Changed role of " + owner.getEmail() + " from " + oldRole + " to " + dto.getRole(),
                request
        );
        return ResponseEntity.ok(ownerMapper.toDto(owner)); // ✅ Using mapper
    }

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
        return ResponseEntity.ok(ownerMapper.toDto(owner)); // ✅ Using mapper
    }

 // ✅ soft delete:
    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id,
                                               @AuthenticationPrincipal OwnerPrincipal principal,
                                               HttpServletRequest request) {
        Owner owner = ownerService.findById(id);
        
        // ✅ Use soft delete (toggle isActive = false)
        ownerService.toggleActive(id);
        
        auditLogService.log(
                ownerService.findById(principal.getId()),
                "DEACTIVATE_USER",
                "Deactivated user: " + owner.getEmail(),
                request
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<AuditLogDto> getAuditLogs(
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return auditLogService.getAllLogs(pageable);
    }
}