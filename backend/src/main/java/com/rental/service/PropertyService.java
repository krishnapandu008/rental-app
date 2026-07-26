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

    // ---------- Public / Visible Listings with filters and pagination ----------
    public Page<PropertyResponseDto> getVisibleProperties(Long ownerId, String role, String location,
                                                          Double minPrice, Double maxPrice, Integer bedrooms,
                                                          Pageable pageable) {
        Page<Property> page = propertyRepository.findVisibleWithFilters(ownerId, role, location, minPrice, maxPrice, bedrooms, pageable);
        return page.map(this::toDto);
    }

    // ---------- Single Property with Access Check ----------
    public PropertyResponseDto getPropertyById(Long id, Long ownerId, String role) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (!accessService.canView(ownerId, role, property)) {
            throw new ForbiddenException("You don't have permission to view this property");
        }
        return toDto(property);
    }

    // ---------- Owner-specific (for their own management) ----------
    public List<PropertyResponseDto> getByOwner(Long ownerId) {
        return propertyRepository.findByOwnerId(ownerId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
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
        Property property = propertyRepository.findByImageUrl(imageUrl)
                .orElseThrow(() -> {
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

    // ---------- DTO conversion ----------
    private PropertyResponseDto toDto(Property property) {
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
        dto.setLatitude(property.getLatitude());
        dto.setLongitude(property.getLongitude());
        return dto;
    }
}