package com.rental.controller;

import com.rental.dto.OwnerSummaryDto;
import com.rental.dto.UpdateRoleRequestDto;
import com.rental.entity.Owner;
import com.rental.exception.ResourceNotFoundException;
import com.rental.repository.OwnerRepository;
import com.rental.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OwnerRepository ownerRepository;
    private final OwnerService ownerService;

    @GetMapping("/users")
    public List<OwnerSummaryDto> getAllUsers() {
        return ownerRepository.findAll().stream()
                .map(o -> new OwnerSummaryDto(o.getId(), o.getEmail(), o.getName(), o.getPhone(), o.getRole()))
                .collect(Collectors.toList());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<OwnerSummaryDto> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequestDto request) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        owner.setRole(request.getRole());
        ownerRepository.save(owner);
        OwnerSummaryDto dto = new OwnerSummaryDto(owner.getId(), owner.getEmail(),
                owner.getName(), owner.getPhone(), owner.getRole());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // You may want to soft-delete (e.g., set active=false) instead of hard delete
        ownerRepository.delete(owner);
        return ResponseEntity.noContent().build();
    }
}