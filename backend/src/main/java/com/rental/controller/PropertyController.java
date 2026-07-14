package com.rental.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rental.dto.PropertyRequestDto;
import com.rental.dto.PropertyResponseDto;
import com.rental.exception.ForbiddenException;
import com.rental.exception.UnauthorizedException;
import com.rental.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

    @GetMapping
    public List<PropertyResponseDto> getAllAvailable() {
        return propertyService.getAllAvailable();
    }

    @GetMapping("/owner/{ownerId}")
    public List<PropertyResponseDto> getByOwner(@PathVariable Long ownerId, @AuthenticationPrincipal String principal) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized: JWT token required");
        }
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long requesterId = Long.valueOf(principal);
        if (!isAdmin && !requesterId.equals(ownerId)) {
            throw new ForbiddenException("Forbidden: You can only view your own properties");
        }
        return propertyService.getByOwner(ownerId);
    }

    // Admin-only: properties across every owner (SecurityConfig restricts this to ROLE_ADMIN)
    @GetMapping("/admin/all")
    public List<PropertyResponseDto> getAllForAdmin() {
        return propertyService.getAllProperties();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal String principal) {
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized: JWT token required");
        }
        Long ownerId = Long.valueOf(principal);
        propertyService.deleteByIdAndOwnerId(id, ownerId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponseDto create(
            @Valid @RequestPart("property") PropertyRequestDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images, 
            @AuthenticationPrincipal String principal) {
        
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized: JWT token required");
        }
        
        Long ownerId = Long.valueOf(principal);
        if (!ownerId.equals(dto.getOwnerId())) {
            throw new ForbiddenException("Forbidden: Cannot create property for another owner");
        }
        return propertyService.create(dto, images);
    }

    @GetMapping("/{id}")
    public PropertyResponseDto getPropertyById(@PathVariable Long id) {
        return propertyService.getPropertyById(id);
    }

    @PutMapping("/{id}")
    public PropertyResponseDto updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequestDto dto,
            @AuthenticationPrincipal String principal) {
        
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized: JWT token required");
        }
        Long ownerId = Long.valueOf(principal);
        return propertyService.update(id, dto, ownerId);
    }

    @PostMapping("/{id}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public List<String> uploadImages(
            @PathVariable Long id,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal String principal) {
        
        if (principal == null) {
            throw new UnauthorizedException("Unauthorized: JWT token required");
        }
        Long ownerId = Long.valueOf(principal);
        return propertyService.uploadImages(id, images, ownerId);
    }
}