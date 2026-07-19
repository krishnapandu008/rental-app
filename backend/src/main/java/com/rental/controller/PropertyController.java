package com.rental.controller;

import java.util.List;
import java.util.Map;  // ✅ NEW IMPORT

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rental.dto.PropertyRequestDto;
import com.rental.dto.PropertyResponseDto;
import com.rental.exception.UnauthorizedException;
import com.rental.security.OwnerPrincipal;
import com.rental.service.PropertyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    // ---------- Public Listing ----------
    @GetMapping
    public List<PropertyResponseDto> getAllProperties(@AuthenticationPrincipal OwnerPrincipal principal) {
        Long ownerId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : null;
        return propertyService.getVisibleProperties(ownerId, role);
    }

    // ---------- Single Property ----------
    @GetMapping("/{id}")
    public PropertyResponseDto getProperty(@PathVariable Long id,
                                           @AuthenticationPrincipal OwnerPrincipal principal) {
        Long ownerId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole() : null;
        return propertyService.getPropertyById(id, ownerId, role);
    }

    // ---------- Owner's Own Properties ----------
    @GetMapping("/owner/{ownerId}")
    public List<PropertyResponseDto> getByOwner(@PathVariable Long ownerId,
                                                @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null ||
            (!principal.getId().equals(ownerId) && !"ADMIN".equalsIgnoreCase(principal.getRole()))) {
            throw new UnauthorizedException("You can only view your own properties");
        }
        return propertyService.getByOwner(ownerId);
    }

    // ---------- Create (with optional images) ----------
    @PostMapping
    public PropertyResponseDto create(@RequestPart("dto") @Valid PropertyRequestDto dto,
                                      @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                      @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        return propertyService.create(dto, principal.getId(), images);
    }

    // ---------- Update ----------
    @PutMapping("/{id}")
    public PropertyResponseDto update(@PathVariable Long id,
                                      @RequestBody @Valid PropertyRequestDto dto,
                                      @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        return propertyService.update(id, dto, principal.getId(), principal.getRole());
    }

    // ---------- Delete Property ----------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        propertyService.delete(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok().build();
    }

    // ---------- Delete Individual Image ----------
    @DeleteMapping("/images")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> payload,
                                         @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        String imageUrl = payload.get("url");
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required");
        }
        propertyService.deleteImage(imageUrl, principal.getId(), principal.getRole());
        return ResponseEntity.ok().build();
    }

    // ---------- Upload extra images ----------
    @PostMapping("/{id}/images")
    public List<String> uploadImages(@PathVariable Long id,
                                     @RequestParam("images") List<MultipartFile> images,
                                     @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        return propertyService.uploadImages(id, images, principal.getId(), principal.getRole());
    }

    // ---------- Admin only: toggle active ----------
    @PatchMapping("/admin/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        propertyService.toggleActive(id, active);
        return ResponseEntity.ok().build();
    }
}