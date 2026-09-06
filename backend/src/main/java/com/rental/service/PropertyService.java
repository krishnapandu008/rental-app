package com.rental.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.rental.entity.Address;
import com.rental.entity.Amenity;
import com.rental.entity.Owner;
import com.rental.entity.Property;
import com.rental.entity.PropertyImage;
import com.rental.exception.ForbiddenException;
import com.rental.exception.ResourceNotFoundException;
import com.rental.location.service.LocationService;
import com.rental.mapper.PropertyMapper;
import com.rental.repository.AddressRepository;
import com.rental.repository.AmenityRepository;
import com.rental.repository.OwnerRepository;
import com.rental.repository.PropertyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final AmenityRepository amenityRepository;
    private final LocalStorageService storageService;
    private final PropertyAccessService accessService;
    private final FavoriteService favoriteService;
    private final LocationService locationService;
    private final PropertyMapper propertyMapper;
    private final LocationManagementService locationManagementService;
    private final AddressRepository addressRepository;
    // ---------- Public / Visible Listings with filters and pagination ----------
    public Page<PropertyResponseDto> getVisibleProperties(Long ownerId, String role, String location, Double minPrice,
        Double maxPrice, Integer bedrooms, List<String> amenities, Pageable pageable) {
    try {
        log.info("Normal Search - Filters: ownerId={}, role={}, location={}, minPrice={}, maxPrice={}, bedrooms={}, amenities={}, pageable={}",
                ownerId, role, location, minPrice, maxPrice, bedrooms, amenities, pageable);

        Long locationId = null;
        if (location != null && !location.isEmpty()) {
            locationId = locationManagementService.getLocationIdByName(location);
            // ✅ If location provided but not found → return empty page
            if (locationId == null) {
                log.info("Location '{}' not found, returning empty page", location);
                return Page.empty(pageable);
            }
        }

        List<Long> amenityIds = null;
        if (amenities != null && !amenities.isEmpty()) {
            amenityIds = amenities.stream()
                .flatMap(value -> java.util.Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(amenityName -> {
                    return amenityRepository.findByAmenityNameAndIsActiveTrue(amenityName)
                        .map(Amenity::getId)
                        .orElse(null);
                })
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
            
            if (amenityIds.isEmpty()) {
                amenityIds = null;
            }
        }

        Page<Property> page = propertyRepository.findVisibleWithFilters(
            ownerId, 
            role, 
            locationId,
            minPrice, 
            maxPrice,
            bedrooms, 
            amenityIds,
            pageable
        );

        log.info("✅ Normal Search - Found {} properties", page.getTotalElements());

        return page.map(propertyMapper::toDto);
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
        
        PropertyResponseDto dto = propertyMapper.toDto(property);
        if (ownerId != null) {
            dto.setFavorited(favoriteService.isFavorited(ownerId, id));
        } else {
            dto.setFavorited(false);
        }
        return dto;
    }

    // ---------- Owner-specific (for their own management) ----------
    public List<PropertyResponseDto> getByOwner(Long ownerId) {
        return propertyRepository.findByOwnerId(ownerId).stream()
                .map(propertyMapper::toDto)
                .collect(Collectors.toList());
    }

    // ---------- Create (with or without images) ----------
    @Transactional
    public PropertyResponseDto create(PropertyRequestDto dto, Long authenticatedOwnerId) {
        return create(dto, authenticatedOwnerId, null);
    }

    @Transactional
    public PropertyResponseDto create(PropertyRequestDto dto, Long authenticatedOwnerId, List<MultipartFile> images) {
        Owner owner = ownerRepository.findById(authenticatedOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with ID: " + authenticatedOwnerId));

        Property property = propertyMapper.toEntity(dto);

        property.setOwner(owner);
        if (dto.getAddressId() != null) {
            Address address = addressRepository.findById(dto.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + dto.getAddressId()));
            property.setAddress(address);
        } else {
            property.setAddress(null); // explicitly clear if no address provided
        }
        property.setIsAvailable(dto.getAvailable() != null ? dto.getAvailable() : true);
        property.setIsActive(true);
        property.setCreatedAt(LocalDateTime.now());

        if (dto.getAmenityIds() != null && !dto.getAmenityIds().isEmpty()) {
            List<Amenity> amenities = amenityRepository.findByIdInAndIsActiveTrue(dto.getAmenityIds());
            if (!amenities.isEmpty()) {
                property.setAmenities(amenities);
            }
        }

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

            if (saved.getImages() == null) {
                saved.setImages(new ArrayList<>());
            }
            for (String url : imageUrls) {
                PropertyImage image = PropertyImage.builder()
                    .property(saved)
                    .imageUrl(url)
                    .isPrimary(saved.getImages().isEmpty())
                    .displayOrder(saved.getImages().size())
                    .build();
                saved.getImages().add(image);
            }
            propertyRepository.save(saved);
        }

        return propertyMapper.toDto(saved);
    }

    // ---------- Update ----------
    @Transactional
    public PropertyResponseDto update(Long id, PropertyRequestDto dto, Long ownerId, String role) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        
        if (!accessService.canManage(ownerId, role, property)) {
            throw new ForbiddenException("You are not allowed to edit this property");
        }

        propertyMapper.updateEntity(property, dto);
     // ✅ P0-5: Handle address update
        if (dto.getAddressId() != null) {
            Address address = addressRepository.findById(dto.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + dto.getAddressId()));
            property.setAddress(address);
        } else {
            property.setAddress(null); // remove address if null
        }
        if (dto.getAmenityIds() != null) {
            if (dto.getAmenityIds().isEmpty()) {
                property.getAmenities().clear();
            } else {
                List<Amenity> amenities = amenityRepository.findByIdInAndIsActiveTrue(dto.getAmenityIds());
                property.setAmenities(amenities);
            }
        }

        propertyRepository.save(property);
        return propertyMapper.toDto(property);
    }

    // ---------- Delete Property (hard delete) ----------
    @Transactional
    public void delete(Long id, Long ownerId, String role) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        
        if (!accessService.canManage(ownerId, role, property)) {
            throw new ForbiddenException("You are not allowed to delete this property");
        }

        if (property.getImages() != null) {
            for (PropertyImage image : property.getImages()) {
                storageService.deleteFile(image.getImageUrl());
            }
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

        if (property.getImages() != null) {
            boolean removed = property.getImages().removeIf(img -> img.getImageUrl().equals(imageUrl));
            log.info("Image removed: {}", removed);
            propertyRepository.save(property);
        }
        storageService.deleteFile(imageUrl);
    }

    // ---------- Admin: soft delete / restore (toggles isActive flag) ----------
    @Transactional
    public void toggleActive(Long id, boolean active) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setIsActive(active);  // ✅ This toggles BaseEntity.isActive (soft delete)
        propertyRepository.save(property);
    }

    // ---------- Toggle availability (toggles isAvailable flag) ----------
    @Transactional
    public void toggleAvailability(Long id, boolean available) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setIsAvailable(available);  // ✅ This toggles the property availability
        propertyRepository.save(property);
    }

    // ---------- Upload additional images ----------
    @Transactional
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

        if (property.getImages() == null) {
            property.setImages(new ArrayList<>());
        }
        for (String url : imageUrls) {
            PropertyImage image = PropertyImage.builder()
                .property(property)
                .imageUrl(url)
                .isPrimary(false)
                .displayOrder(property.getImages().size())
                .build();
            property.getImages().add(image);
        }
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
                PropertyResponseDto dto = propertyMapper.toDto(property);
                dto.setDistance(distance);
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<PropertyResponseDto> getAllPropertiesWithCoordinates() {
        return propertyRepository.findAllWithCoordinates().stream()
            .map(propertyMapper::toDto)
            .collect(Collectors.toList());
    }

    public List<String> getLocationSuggestions(String query) {
        return locationService.getLocationSuggestions(query);
    }

    public Map<String, Object> getLocationInfo() {
        return locationService.getLocationInfo();
    }

    public boolean isLocationSupported(String query) {
        return locationService.isLocationSupported(query);
    }

    public String getLocationDisplay() {
        return locationService.getLocationDisplay();
    }
}