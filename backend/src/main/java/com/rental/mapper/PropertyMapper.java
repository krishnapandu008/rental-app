package com.rental.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.rental.dto.PropertyRequestDto;
import com.rental.dto.PropertyResponseDto;
import com.rental.entity.Property;
import com.rental.mapper.base.BaseMapper;

/**
 * Mapper for Property entity ↔ Property DTOs
 * Auto-maps fields with same names, only explicit mappings needed for different names
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        LocationMapper.class,
        AddressMapper.class,
        PropertyTypeMapper.class,
        AmenityMapper.class,
        PropertyImageMapper.class,
        OwnerMapper.class
    }
)
public interface PropertyMapper extends BaseMapper<Property, PropertyResponseDto> {

    // ================================================================
    // PROPERTY → DTO (Auto-maps same name fields)
    // ================================================================

    @Override
    @Mapping(source = "isAvailable", target = "available")
    PropertyResponseDto toDto(Property property);

    // ================================================================
    // REQUEST DTO → PROPERTY (Only fields with different names)
    // ================================================================

    @Mapping(source = "locationId", target = "location.id")
    @Mapping(source = "propertyTypeId", target = "propertyType.id")
    @Mapping(target = "address", ignore = true)
    @Mapping(source = "available", target = "isAvailable")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "locationName", ignore = true)
    Property toEntity(PropertyRequestDto dto);

    // ✅ Update existing property from request DTO (Partial update)
    @Mapping(source = "locationId", target = "location.id")
    @Mapping(source = "propertyTypeId", target = "propertyType.id")
    @Mapping(target = "address", ignore = true)
    @Mapping(source = "available", target = "isAvailable")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "locationName", ignore = true)
    void updateEntity(@MappingTarget Property property, PropertyRequestDto dto);

    // ================================================================
    // HELPER METHODS (For custom mapping logic)
    // ================================================================

    /**
     * Helper method to get location display name
     */
    default String mapLocationName(Property property) {
        if (property.getLocation() != null) {
            return property.getLocation().getDisplayName();
        }
        return property.getLocationName();
    }

    /**
     * Helper method to get amenity IDs from property
     */
    default List<Long> mapAmenityIds(Property property) {
        if (property.getAmenities() != null) {
            return property.getAmenities().stream()
                .map(amenity -> amenity.getId())
                .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    /**
     * Helper method to get image IDs from property
     */
    default List<Long> mapImageIds(Property property) {
        if (property.getImages() != null) {
            return property.getImages().stream()
                .map(image -> image.getId())
                .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}