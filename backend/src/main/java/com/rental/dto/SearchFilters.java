package com.rental.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilters {
    private Long locationId;
    private Double minRent;
    private Double maxRent;
    private Integer bedrooms;
    private Double bathrooms;
    private List<Long> amenityIds;      // ✅ Changed from 'amenities' to 'amenityIds'
    private String explanation;

    // ✅ If you need backward compatibility, add this getter
    @Deprecated
    public List<Long> getAmenities() {
        return amenityIds;
    }

    // ✅ If you need backward compatibility, add this setter
    @Deprecated
    public void setAmenities(List<Long> amenities) {
        this.amenityIds = amenities;
    }
}