package com.rental.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rental.enums.Visibility;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PropertyResponseDto {
    private Long id;
    private String title;
    private String description;
    
    // ✅ New entity references
    private LocationResponseDto location;
    private AddressResponseDto address;
    private PropertyTypeResponseDto propertyType;
    private OwnerSummaryDto owner;
    
    private Double rent;
    private Integer bedrooms;
    private Double bathrooms;
    private Integer squareFeet;
    private String contactNumber;
    private Boolean available;
    
    private List<AmenityResponseDto> amenities;      // ✅ Changed from List<String>
    private List<ImageResponseDto> images;           // ✅ Changed from List<String>
    
    private Visibility visibility;
    @JsonProperty("isActive")
    private boolean isActive;
    
    private Double latitude;
    private Double longitude;
    private boolean isFavorited;
    private Double distance;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}