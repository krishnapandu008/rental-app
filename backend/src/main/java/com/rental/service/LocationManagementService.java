package com.rental.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rental.dto.LocationResponseDto;
import com.rental.entity.Amenity;
import com.rental.entity.Location;
import com.rental.exception.ResourceNotFoundException;
import com.rental.mapper.LocationMapper;
import com.rental.repository.AmenityRepository;
import com.rental.repository.LocationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional  // ✅ Added at class level for all write operations
public class LocationManagementService {

    private final LocationRepository locationRepository;
    private final AmenityRepository amenityRepository;
    private final LocationMapper locationMapper;

    // ================================================================
    // CREATE
    // ================================================================

    public LocationResponseDto createLocation(LocationResponseDto locationDto) {
        log.info("Creating new location: {}", locationDto.getDisplayName());
        
        // Check if locationId already exists
        if (locationRepository.findByLocationId(locationDto.getLocationId()).isPresent()) {
            throw new RuntimeException("Location ID already exists: " + locationDto.getLocationId());
        }

        Location location = locationMapper.toEntity(locationDto);
        location.setIsActive(true);

        // Set default values if not provided
        if (location.getDisplayOrder() == null) {
            location.setDisplayOrder(0);
        }
        if (location.getIsDefault() == null) {
            location.setIsDefault(false);
        }

        // If this is set as default, remove default from others
        if (Boolean.TRUE.equals(location.getIsDefault())) {
            locationRepository.findAllByIsActiveTrue().forEach(loc -> {
                loc.setIsDefault(false);
                locationRepository.save(loc);
            });
        }

        Location savedLocation = locationRepository.save(location);
        log.info("✅ Location created successfully with ID: {}", savedLocation.getId());

        return locationMapper.toDto(savedLocation);
    }

    // ================================================================
    // FIND LOCATIONS
    // ================================================================

    public LocationResponseDto getLocationById(Long id) {
        log.info("Getting location by ID: {}", id);
        Location location = locationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));
        return locationMapper.toDto(location);
    }

    public LocationResponseDto getLocationByLocationId(String locationId) {
        log.info("Getting location by locationId: {}", locationId);
        Location location = locationRepository.findByLocationIdAndIsActiveTrue(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + locationId));
        return locationMapper.toDto(location);
    }

    public LocationResponseDto getDefaultLocation() {
        log.info("Getting default location");
        Location location = locationRepository.findByIsDefaultTrueAndIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No default location found"));
        return locationMapper.toDto(location);
    }

    public Page<LocationResponseDto> getAllLocations(Pageable pageable) {
        log.info("📋 Getting all locations with pagination");
        return locationRepository.findAllByIsActiveTrue(pageable)
                .map(locationMapper::toDto);
    }

    public List<LocationResponseDto> getActiveLocations() {
        log.info("📋 Getting all active locations");
        return locationRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(locationMapper::toDto)
                .collect(Collectors.toList());
    }

    // ================================================================
    // SEARCH LOCATIONS
    // ================================================================

    public List<LocationResponseDto> searchLocations(String query) {
        log.info("Searching locations by: {}", query);
        return locationRepository.findByDisplayNameContainingIgnoreCase(query)
                .stream()
                .map(locationMapper::toDto)
                .collect(Collectors.toList());
    }

    public Page<LocationResponseDto> searchLocations(String query, Boolean isActive, Pageable pageable) {
        log.info("Searching locations by: {}, active: {}", query, isActive);
        return locationRepository.searchLocations(query, isActive, pageable)
                .map(locationMapper::toDto);
    }

    // ================================================================
    // SUGGESTIONS / AUTOCOMPLETE
    // ================================================================

    public List<String> getLocationSuggestions(String query) {
        log.info("💡 Getting location suggestions for: {}", query);
        return locationRepository.findLocationSuggestions(query);
    }

    public List<String> getLocationSuggestionsContaining(String query) {
        log.info("💡 Getting location suggestions (containing) for: {}", query);
        return locationRepository.findLocationSuggestionsContaining(query);
    }

    // ================================================================
    // UPDATE LOCATIONS
    // ================================================================

    public LocationResponseDto updateLocation(Long id, LocationResponseDto locationDto) {
        log.info("✏️ Updating location with ID: {}", id);

        Location location = locationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));

        // Update fields only if provided in DTO
        if (locationDto.getDisplayName() != null) {
            location.setDisplayName(locationDto.getDisplayName());
        }
        if (locationDto.getDistrict() != null) {
            location.setDistrict(locationDto.getDistrict());
        }
        if (locationDto.getState() != null) {
            location.setState(locationDto.getState());
        }
        if (locationDto.getCountry() != null) {
            location.setCountry(locationDto.getCountry());
        }
        if (locationDto.getPinCode() != null) {
            location.setPinCode(locationDto.getPinCode());
        }
        if (locationDto.getLatitude() != null) {
            location.setLatitude(locationDto.getLatitude());
        }
        if (locationDto.getLongitude() != null) {
            location.setLongitude(locationDto.getLongitude());
        }
        if (locationDto.getDisplayOrder() != null) {
            location.setDisplayOrder(locationDto.getDisplayOrder());
        }
        if (locationDto.getIsActive() != null) {
            location.setIsActive(locationDto.getIsActive());
        }

        // Handle default flag carefully
        if (Boolean.TRUE.equals(locationDto.getIsDefault())) {
            // Reset all other defaults
            locationRepository.findAllByIsActiveTrue().forEach(loc -> {
                if (!loc.getId().equals(id)) {
                    loc.setIsDefault(false);
                    locationRepository.save(loc);
                }
            });
            location.setIsDefault(true);
        } else if (Boolean.FALSE.equals(locationDto.getIsDefault())) {
            // Only allow setting to false if there's another default
            long defaultCount = locationRepository.countDefaultLocations();
            if (defaultCount <= 1 && Boolean.TRUE.equals(location.getIsDefault())) {
                throw new RuntimeException("Cannot remove default status. There must be at least one default location.");
            }
            location.setIsDefault(false);
        }

        Location updatedLocation = locationRepository.save(location);
        log.info("✅ Location updated successfully with ID: {}", updatedLocation.getId());

        return locationMapper.toDto(updatedLocation);
    }

    public LocationResponseDto setDefaultLocation(Long id) {
        log.info("Setting default location: {}", id);
        
        // Reset all defaults
        locationRepository.findAllByIsActiveTrue().forEach(loc -> {
            loc.setIsDefault(false);
            locationRepository.save(loc);
        });

        // Set new default
        Location location = locationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));

        location.setIsDefault(true);
        Location updatedLocation = locationRepository.save(location);
        log.info("✅ Default location set to: {}", updatedLocation.getDisplayName());

        return locationMapper.toDto(updatedLocation);
    }

    // ================================================================
    // SOFT DELETE / RESTORE
    // ================================================================

    public void softDeleteLocation(Long id) {
        log.info("🗑️ Soft deleting location with ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));

        // Don't allow deleting default location
        if (Boolean.TRUE.equals(location.getIsDefault())) {
            throw new RuntimeException("Cannot delete default location. Please set another location as default first.");
        }

        location.setIsActive(false);
        locationRepository.save(location);
        log.info("✅ Location soft deleted successfully");
    }

    public void restoreLocation(Long id) {
        log.info("🔄 Restoring location with ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));

        location.setIsActive(true);
        locationRepository.save(location);
        log.info("✅ Location restored successfully");
    }

    // ================================================================
    // STATISTICS
    // ================================================================

    public long getActiveLocationCount() {
        return locationRepository.countActiveLocations();
    }

    public long getDefaultLocationCount() {
        return locationRepository.countDefaultLocations();
    }

    // ================================================================
    // HELPER METHODS (For internal use)
    // ================================================================

    public Location getLocationEntityById(Long id) {
        return locationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));
    }

    public boolean existsByLocationId(String locationId) {
        return locationRepository.findByLocationId(locationId).isPresent();
    }

    public Long getLocationIdByName(String locationName) {
        if (locationName == null || locationName.isEmpty()) {
            return null;
        }
        
        List<Location> locations = locationRepository.findByDisplayNameContainingIgnoreCase(locationName);
        return locations.stream()
            .findFirst()
            .map(Location::getId)
            .orElse(null);
    }

    public Long getAmenityIdByName(String amenityName) {
        if (amenityName == null || amenityName.isEmpty()) {
            return null;
        }
        
        return amenityRepository.findByAmenityNameAndIsActiveTrue(amenityName)
            .map(Amenity::getId)
            .orElse(null);
    }

    public LocationResponseDto getLocationByDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        
        return locationRepository.findByDisplayNameContainingIgnoreCase(displayName)
            .stream()
            .findFirst()
            .map(locationMapper::toDto)
            .orElse(null);
    }
}