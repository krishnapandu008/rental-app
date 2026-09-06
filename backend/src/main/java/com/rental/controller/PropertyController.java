package com.rental.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    // ================================================================
    // ✅ GET without path - List all properties
    // ================================================================
    @GetMapping
    public Page<PropertyResponseDto> getAllProperties(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long ownerId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole().name() : null;

        log.info("📍 Location filter: {}", location);

        Sort sort = Sort.by("createdAt").descending();
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc":
                    sort = Sort.by("rent").ascending();
                    break;
                case "price_desc":
                    sort = Sort.by("rent").descending();
                    break;
                case "newest":
                    sort = Sort.by("createdAt").descending();
                    break;
                default:
                    sort = Sort.by("createdAt").descending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return propertyService.getVisibleProperties(ownerId, role, location, minPrice, maxPrice, bedrooms, amenities, pageable);
    }

    // ================================================================
    // ✅ GET specific endpoints (MUST be BEFORE /{id})
    // ================================================================

    @GetMapping("/nearby")
    public ResponseEntity<List<PropertyResponseDto>> getNearbyProperties(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10") Double radiusKm,
            @RequestParam(required = false) Double minRent,
            @RequestParam(required = false) Double maxRent,
            @RequestParam(required = false) Integer bedrooms) {
        
        List<PropertyResponseDto> properties = 
            propertyService.findNearbyProperties(lat, lng, radiusKm, minRent, maxRent, bedrooms);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/map")
    public ResponseEntity<List<PropertyResponseDto>> getPropertiesForMap() {
        return ResponseEntity.ok(propertyService.getAllPropertiesWithCoordinates());
    }

    @GetMapping("/locations/suggest")
    public ResponseEntity<List<String>> getLocationSuggestions(@RequestParam String q) {
        log.info("📍 Getting location suggestions for: '{}' (Database)", q);
        List<String> suggestions = propertyService.getLocationSuggestions(q);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/search-locations")
    public ResponseEntity<List<String>> searchLocations(@RequestParam String q) {
        log.info("🤖 AI-Powered location search for: '{}'", q);
        List<String> suggestions = propertyService.getLocationSuggestions(q);
        return ResponseEntity.ok(suggestions);
    }

    // --- 🔒 AUTHENTICATED endpoints ---

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("isAuthenticated()")
    public List<PropertyResponseDto> getByOwner(@PathVariable Long ownerId,
                                                @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null ||
            (!principal.getId().equals(ownerId) &&
             !"ADMIN".equalsIgnoreCase(principal.getRole().name()) &&
             !"SUPER_ADMIN".equalsIgnoreCase(principal.getRole().name()))) {   // added SUPER_ADMIN
            throw new UnauthorizedException("You can only view your own properties");
        }
        return propertyService.getByOwner(ownerId);
    }

    // ================================================================
    // ✅ Admin endpoints
    // ================================================================

    @PatchMapping("/admin/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        propertyService.toggleActive(id, active);
        return ResponseEntity.ok().build();
    }

    // ================================================================
    // ✅ DELETE specific endpoints (BEFORE /{id})
    // ================================================================

    @DeleteMapping("/images")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> payload,
                                         @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        String imageUrl = payload.get("url");
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required");
        }
        propertyService.deleteImage(imageUrl, principal.getId(), principal.getRole().name());
        return ResponseEntity.ok().build();
    }

    // ================================================================
    // ✅ POST - Create (no path variable)
    // ================================================================

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public PropertyResponseDto create(@RequestPart("dto") @Valid PropertyRequestDto dto,
                                      @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                      @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        return propertyService.create(dto, principal.getId(), images);
    }

    // ================================================================
    // ✅ CRUD endpoints with {id} (MUST BE LAST)
    // ================================================================

    @GetMapping("/{id}")
    public PropertyResponseDto getProperty(@PathVariable Long id,
                                           @AuthenticationPrincipal OwnerPrincipal principal) {
        Long ownerId = principal != null ? principal.getId() : null;
        String role = principal != null ? principal.getRole().name() : null;
        return propertyService.getPropertyById(id, ownerId, role);
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("isAuthenticated()")
    public List<String> uploadImages(@PathVariable Long id,
                                     @RequestParam("images") List<MultipartFile> images,
                                     @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        return propertyService.uploadImages(id, images, principal.getId(), principal.getRole().name());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PropertyResponseDto update(@PathVariable Long id,
                                      @RequestBody @Valid PropertyRequestDto dto,
                                      @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        return propertyService.update(id, dto, principal.getId(), principal.getRole().name());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal OwnerPrincipal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        propertyService.delete(id, principal.getId(), principal.getRole().name());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/location-info")
    public ResponseEntity<Map<String, Object>> getLocationInfo() {
        return ResponseEntity.ok(propertyService.getLocationInfo());
    }

    @GetMapping("/location-supported")
    public ResponseEntity<Map<String, Boolean>> isLocationSupported(@RequestParam String q) {
        return ResponseEntity.ok(Map.of("supported", propertyService.isLocationSupported(q)));
    }

    @GetMapping("/location-display")
    public ResponseEntity<Map<String, String>> getLocationDisplay() {
        return ResponseEntity.ok(Map.of("display", propertyService.getLocationDisplay()));
    }
}