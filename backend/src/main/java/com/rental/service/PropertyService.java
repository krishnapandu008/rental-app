package com.rental.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rental.dto.PropertyRequestDto;
import com.rental.dto.PropertyResponseDto;
import com.rental.entity.Property;
import com.rental.enums.Visibility;
import com.rental.exception.ForbiddenException;
import com.rental.exception.ResourceNotFoundException;
import com.rental.repository.PropertyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    private final PropertyRepository propertyRepository;
    private final LocalStorageService storageService;
    private final PropertyAccessService accessService;
    private final FavoriteService favoriteService;

    // ---------- Public / Visible Listings with filters and pagination ----------
    public Page<PropertyResponseDto> getVisibleProperties(Long ownerId, String role, String location, Double minPrice,
            Double maxPrice, Integer bedrooms, List<String> amenities, Pageable pageable) {
        try {
            // ✅ ADD DEBUG LOGGING
            log.info("🔍 Normal Search - Filters: ownerId={}, role={}, location={}, minPrice={}, maxPrice={}, bedrooms={}, amenities={}, pageable={}",
                ownerId, role, location, minPrice, maxPrice, bedrooms, amenities, pageable);
            
            List<String> amenityFilter = null;
            if (amenities != null && !amenities.isEmpty()) {
                amenityFilter = amenities.stream()
                    .flatMap(value -> java.util.Arrays.stream(value.split(",")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .toList();
                if (amenityFilter.isEmpty()) {
                    amenityFilter = null;
                }
            }
            Page<Property> page = propertyRepository.findVisibleWithFilters(ownerId, role, location, minPrice, maxPrice,
                    bedrooms, amenityFilter, pageable);
            
            log.info("✅ Normal Search - Found {} properties", page.getTotalElements());
            
            return page.map(property -> {
                PropertyResponseDto dto = toDto(property);
                if (ownerId != null) {
                    dto.setFavorited(favoriteService.isFavorited(ownerId, property.getId()));
                } else {
                    dto.setFavorited(false);
                }
                return dto;
            });
        } catch (Exception e) {
            log.error("Error fetching properties with filters...", e);
            throw e;
        }
    }

    // ---------- Single Property with Access Check ----------
    public PropertyResponseDto getPropertyById(Long id, Long ownerId, String role) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (!accessService.canView(ownerId, role, property)) {
            throw new ForbiddenException("You don't have permission to view this property");
        }
        PropertyResponseDto dto = toDto(property);
        if (ownerId != null) {
            dto.setFavorited(favoriteService.isFavorited(ownerId, id));
        } else {
            dto.setFavorited(false);
        }
        return dto;
    }

    // ---------- Owner-specific (for their own management) ----------
    public List<PropertyResponseDto> getByOwner(Long ownerId) {
        return propertyRepository.findByOwnerId(ownerId).stream().map(this::toDto).collect(Collectors.toList());
    }

    // ---------- Create (with or without images) ----------
    public PropertyResponseDto create(PropertyRequestDto dto, Long authenticatedOwnerId) {
        return create(dto, authenticatedOwnerId, null);
    }

    public PropertyResponseDto create(PropertyRequestDto dto, Long authenticatedOwnerId, List<MultipartFile> images) {
        Property property = Property.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .rent(dto.getRent())
                .bedrooms(dto.getBedrooms())
                .contactNumber(dto.getContactNumber())
                .ownerId(authenticatedOwnerId)
                .available(dto.getAvailable() != null ? dto.getAvailable() : true)
                .visibility(dto.getVisibility() != null ? dto.getVisibility() : Visibility.PUBLIC)
                .amenities(dto.getAmenities())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        Property saved = propertyRepository.save(property);

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                try {
                    String url = storageService.saveFile(image);
                    imageUrls.add(url);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image", e);
                }
            }
            saved.setImageUrls(imageUrls);
            propertyRepository.save(saved);
        }

        return toDto(saved);
    }

    // ---------- Update ----------
    public PropertyResponseDto update(Long id, PropertyRequestDto dto, Long ownerId, String role) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (!accessService.canManage(ownerId, role, property)) {
            throw new ForbiddenException("You are not allowed to edit this property");
        }

        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setLocation(dto.getLocation());
        property.setRent(dto.getRent());
        property.setBedrooms(dto.getBedrooms());
        property.setContactNumber(dto.getContactNumber());
        property.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : property.getAvailable());
        if (dto.getVisibility() != null) {
            property.setVisibility(dto.getVisibility());
        }
        property.setLatitude(dto.getLatitude());
        property.setLongitude(dto.getLongitude());
        if (dto.getAmenities() != null) {
            property.setAmenities(dto.getAmenities());
        }

        propertyRepository.save(property);
        return toDto(property);
    }

    // ---------- Delete Property (hard delete) ----------
    public void delete(Long id, Long ownerId, String role) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (!accessService.canManage(ownerId, role, property)) {
            throw new ForbiddenException("You are not allowed to delete this property");
        }
        for (String url : property.getImageUrls()) {
            storageService.deleteFile(url);
        }
        propertyRepository.delete(property);
    }

    // ---------- Delete Individual Image ----------
    @Transactional
    public void deleteImage(String imageUrl, Long ownerId, String role) {
        log.info("Attempting to delete image: {}", imageUrl);
        Property property = propertyRepository.findByImageUrl(imageUrl).orElseThrow(() -> {
            log.warn("No property found with image URL: {}", imageUrl);
            return new ResourceNotFoundException("Image not found");
        });
        if (!accessService.canManage(ownerId, role, property)) {
            throw new ForbiddenException("You are not allowed to delete this image");
        }
        if (property.getImageUrls() != null) {
            boolean removed = property.getImageUrls().remove(imageUrl);
            log.info("Image removed from list: {}", removed);
            propertyRepository.save(property);
        }
        storageService.deleteFile(imageUrl);
    }

    // ---------- Admin: soft delete / restore ----------
    @Transactional
    public void toggleActive(Long id, boolean active) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setActive(active);
        propertyRepository.save(property);
    }

    // ---------- Upload additional images ----------
    public List<String> uploadImages(Long propertyId, List<MultipartFile> images, Long ownerId, String role) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (!accessService.canManage(ownerId, role, property)) {
            throw new ForbiddenException("You can only add images to your own properties");
        }
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : images) {
            try {
                String url = storageService.saveFile(file);
                imageUrls.add(url);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }
        if (property.getImageUrls() == null) {
            property.setImageUrls(new ArrayList<>());
        }
        property.getImageUrls().addAll(imageUrls);
        propertyRepository.save(property);
        return imageUrls;
    }

    /**
     * Get the Property entity by ID (for internal use)
     */
    public Property getPropertyEntityById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
    }

    public List<PropertyResponseDto> findNearbyProperties(
            Double lat, Double lng, Double radiusKm,
            Double minRent, Double maxRent, Integer bedrooms) {
        
        Double radiusInMeters = radiusKm * 1000;
        
        List<Object[]> results;
        
        if (minRent != null || maxRent != null || bedrooms != null) {
            results = propertyRepository.findNearbyPropertiesWithFilters(
                lat, lng, radiusInMeters, minRent, maxRent, bedrooms
            );
        } else {
            results = propertyRepository.findNearbyProperties(lat, lng, radiusInMeters);
        }
        
        return results.stream()
            .map(row -> {
                Property property = (Property) row[0];
                Double distance = (Double) row[1];
                PropertyResponseDto dto = toDto(property);
                dto.setDistance(distance);
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<PropertyResponseDto> getAllPropertiesWithCoordinates() {
        return propertyRepository.findAllWithCoordinates().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    public PropertyResponseDto toDto(Property property) {
        if (property == null) return null;
        
        PropertyResponseDto dto = new PropertyResponseDto();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setLocation(property.getLocation());
        dto.setRent(property.getRent());
        dto.setBedrooms(property.getBedrooms());
        dto.setContactNumber(property.getContactNumber());
        dto.setAvailable(property.getAvailable());
        dto.setImageUrls(property.getImageUrls());
        dto.setOwnerId(property.getOwnerId());
        dto.setVisibility(property.getVisibility());
        dto.setActive(property.isActive());
        dto.setAmenities(property.getAmenities());
        dto.setLatitude(property.getLatitude());
        dto.setLongitude(property.getLongitude());
        return dto;
    }
    
    // ✅ NEW: Get location suggestions for autocomplete
    public List<String> getLocationSuggestions(String query) {
        if (query == null || query.length() < 2) {
            return List.of();
        }
        return propertyRepository.findDistinctLocationsStartingWith(query)
            .stream()
            .limit(10)
            .collect(Collectors.toList());
    }
    
}