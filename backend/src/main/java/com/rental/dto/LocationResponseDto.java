package com.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDto {
    private Long id;
    private String locationId;
    private String displayName;
    private String district;
    private String state;
    private String country;
    private String pinCode;
    private Double latitude;
    private Double longitude;
    private Integer displayOrder;  // ✅ Added for ordering
    private Boolean isDefault;     // ✅ Added for default location
    private Boolean isActive;      // ✅ Added for active status
}