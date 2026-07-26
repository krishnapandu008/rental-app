package com.rental.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rental.enums.Visibility;

import lombok.Data;

@Data
public class PropertyResponseDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Double rent;
    private Integer bedrooms;
    private String contactNumber;
    private Boolean available;
    private List<String> imageUrls;
    private Long ownerId;
    private Visibility visibility;
    @JsonProperty("isActive")
    private boolean isActive;
    private Double latitude;   // ✅ NEW
    private Double longitude;  // ✅ NEW
}