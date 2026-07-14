package com.rental.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rental.dto.PropertyRequestDto;
import com.rental.dto.PropertyResponseDto;
import com.rental.entity.Property;
import com.rental.exception.ForbiddenException;
import com.rental.exception.ResourceNotFoundException;
import com.rental.exception.UnauthorizedException;
import com.rental.repository.PropertyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyService {
	private final PropertyRepository propertyRepository;
	private final LocalStorageService storageService;

	public List<PropertyResponseDto> getAllAvailable() {
		return propertyRepository.findByAvailableTrue().stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<PropertyResponseDto> getByOwner(Long ownerId) {
		return propertyRepository.findByOwnerId(ownerId).stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<PropertyResponseDto> getAllProperties() {
		return propertyRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
	}

	public PropertyResponseDto create(PropertyRequestDto dto) {
		Property property = Property.builder().title(dto.getTitle()).description(dto.getDescription())
				.location(dto.getLocation()).rent(dto.getRent()).bedrooms(dto.getBedrooms())
				.contactNumber(dto.getContactNumber()).ownerId(dto.getOwnerId()).available(true)
				.createdAt(LocalDateTime.now()).build();
		return toDto(propertyRepository.save(property));
	}

	public void delete(Long id) {
		if (!propertyRepository.existsById(id))
			throw new ResourceNotFoundException("Property not found with id: " + id);
		propertyRepository.deleteById(id);
	}

	public void deleteByIdAndOwnerId(Long id, Long ownerId) {
		Property property = propertyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
		if (!property.getOwnerId().equals(ownerId)) {
			throw new ForbiddenException("Forbidden: You can only delete your own properties");
		}
		propertyRepository.deleteById(id);
	}

	public PropertyResponseDto create(PropertyRequestDto dto, List<MultipartFile> images) {
		Property property = Property.builder().title(dto.getTitle()).description(dto.getDescription())
				.location(dto.getLocation()).rent(dto.getRent()).bedrooms(dto.getBedrooms())
				.contactNumber(dto.getContactNumber()).ownerId(dto.getOwnerId()).available(true)
				.createdAt(LocalDateTime.now()).build();

		// Save property first to get an ID (though not needed for images)
		Property saved = propertyRepository.save(property);

		// Upload images
		List<String> imageUrls = new ArrayList<>();
		if (images != null && !images.isEmpty()) {
			for (MultipartFile image : images) {
				try {
					String url = storageService.saveFile(image);
					imageUrls.add(url);
				} catch (IOException e) {
					throw new RuntimeException("Failed to upload image", e);
				}
			}
		}
		saved.setImageUrls(imageUrls);
		propertyRepository.save(saved);

		return toDto(saved);
	}

	public void delete(Long id, Long ownerId) {
		Property property = propertyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Property not found"));
		if (!property.getOwnerId().equals(ownerId)) {
			throw new UnauthorizedException("Unauthorized");
		}
		// Delete images from disk
		for (String url : property.getImageUrls()) {
			storageService.deleteFile(url);
		}
		propertyRepository.delete(property);
	}

	public PropertyResponseDto update(Long id, PropertyRequestDto dto, Long ownerId) {
		Property property = propertyRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Property not found"));
		if (!property.getOwnerId().equals(ownerId)) {
			throw new ForbiddenException("Forbidden: You can only edit your own properties");
		}
		// Update fields
		property.setTitle(dto.getTitle());
		property.setDescription(dto.getDescription());
		property.setLocation(dto.getLocation());
		property.setRent(dto.getRent());
		property.setBedrooms(dto.getBedrooms());
		property.setContactNumber(dto.getContactNumber());
		// available field? you might allow toggling, but we keep it as is
		property.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : property.getAvailable());
		propertyRepository.save(property);
		return toDto(property);
	}
	public PropertyResponseDto getPropertyById(Long id) {
	    Property property = propertyRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
	    return toDto(property);
	}
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
	    return dto;
	}
	public List<String> uploadImages(Long propertyId, List<MultipartFile> images, Long ownerId) {
	    Property property = propertyRepository.findById(propertyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
	    if (!property.getOwnerId().equals(ownerId)) {
	        throw new ForbiddenException("Forbidden: You can only add images to your own properties");
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
}