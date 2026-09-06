package com.rental.dto;

import java.util.List;

import com.rental.enums.Visibility;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PropertyRequestDto {
    @NotBlank private String title;
    private String description;
    
    @NotNull private Long locationId;      // ✅ Changed from String location
    @NotNull private Long propertyTypeId;  // ✅ NEW
    private Long addressId;                 // ✅ NEW
    
    @NotNull @Positive private Double rent;
    @Min(1) private Integer bedrooms;
    private Double bathrooms;               // ✅ NEW
    private Integer squareFeet;             // ✅ NEW
    
    @NotBlank private String contactNumber;
    private Boolean available;
    private Visibility visibility;
    
    private List<Long> amenityIds;          // ✅ Changed from List<String>
    private List<Long> imageIds;            // ✅ NEW
    
    private Double latitude;
    private Double longitude;
}