package com.rental.controller;

import com.rental.dto.LocationResponseDto;
import com.rental.service.LocationManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationManagementService locationService;

    // ================================================================
    // CREATE
    // ================================================================

    @PostMapping
    public ResponseEntity<LocationResponseDto> createLocation(@Valid @RequestBody LocationResponseDto locationDto) {
        log.info("📍 Create location request: {}", locationDto.getDisplayName());
        LocationResponseDto response = locationService.createLocation(locationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ================================================================
    // GET LOCATIONS
    // ================================================================

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDto> getLocationById(@PathVariable Long id) {
        log.info("🔍 Get location by ID: {}", id);
        LocationResponseDto response = locationService.getLocationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{locationId}")
    public ResponseEntity<LocationResponseDto> getLocationByLocationId(@PathVariable String locationId) {
        log.info("🔍 Get location by locationId: {}", locationId);
        LocationResponseDto response = locationService.getLocationByLocationId(locationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/default")
    public ResponseEntity<LocationResponseDto> getDefaultLocation() {
        log.info("📍 Get default location");
        LocationResponseDto response = locationService.getDefaultLocation();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<LocationResponseDto>> getAllLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LocationResponseDto> response = locationService.getAllLocations(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<LocationResponseDto>> getActiveLocations() {
        log.info("📋 Get all active locations");
        List<LocationResponseDto> response = locationService.getActiveLocations();
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // SEARCH & SUGGESTIONS
    // ================================================================

    @GetMapping("/search")
    public ResponseEntity<List<LocationResponseDto>> searchLocations(@RequestParam String q) {
        log.info("🔍 Search locations: {}", q);
        List<LocationResponseDto> response = locationService.searchLocations(q);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getLocationSuggestions(@RequestParam String q) {
        log.info("💡 Location suggestions for: {}", q);
        List<String> response = locationService.getLocationSuggestions(q);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions/containing")
    public ResponseEntity<List<String>> getLocationSuggestionsContaining(@RequestParam String q) {
        log.info("💡 Location suggestions (containing) for: {}", q);
        List<String> response = locationService.getLocationSuggestionsContaining(q);
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // UPDATE LOCATIONS
    // ================================================================

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDto> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationResponseDto locationDto) {
        log.info("✏️ Update location: {}", id);
        LocationResponseDto response = locationService.updateLocation(id, locationDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<LocationResponseDto> setDefaultLocation(@PathVariable Long id) {
        log.info("📍 Set default location: {}", id);
        LocationResponseDto response = locationService.setDefaultLocation(id);
        return ResponseEntity.ok(response);
    }

    // ================================================================
    // SOFT DELETE / RESTORE
    // ================================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteLocation(@PathVariable Long id) {
        log.info("🗑️ Soft delete location: {}", id);
        locationService.softDeleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreLocation(@PathVariable Long id) {
        log.info("🔄 Restore location: {}", id);
        locationService.restoreLocation(id);
        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // STATISTICS
    // ================================================================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getLocationStats() {
        log.info("📊 Getting location statistics");
        Map<String, Long> stats = Map.of(
            "activeLocations", locationService.getActiveLocationCount(),
            "defaultLocations", locationService.getDefaultLocationCount()
        );
        return ResponseEntity.ok(stats);
    }
}