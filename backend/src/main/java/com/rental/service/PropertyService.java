package com.rental.service;

import com.rental.dto.PropertyRequestDto;
import com.rental.dto.PropertyResponseDto;
import com.rental.entity.Property;
import com.rental.exception.ResourceNotFoundException;
import com.rental.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;

    public List<PropertyResponseDto> getAllAvailable() {
        return propertyRepository.findByAvailableTrue()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<PropertyResponseDto> getByOwner(Long ownerId) {
        return propertyRepository.findByOwnerId(ownerId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public PropertyResponseDto create(PropertyRequestDto dto) {
        Property property = Property.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .rent(dto.getRent())
                .bedrooms(dto.getBedrooms())
                .contactNumber(dto.getContactNumber())
                .ownerId(dto.getOwnerId())
                .available(true)
                .createdAt(LocalDateTime.now())
                .build();
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
            throw new RuntimeException("Forbidden: You can only delete your own properties");
        }
        propertyRepository.deleteById(id);
    }

    private PropertyResponseDto toDto(Property p) {
        PropertyResponseDto dto = new PropertyResponseDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setLocation(p.getLocation());
        dto.setRent(p.getRent());
        dto.setBedrooms(p.getBedrooms());
        dto.setContactNumber(p.getContactNumber());
        dto.setAvailable(p.getAvailable());
        return dto;
    }
}