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
    private String location;
    private Double minRent;
    private Double maxRent;
    private Integer bedrooms;
    private List<String> amenities;
    private String explanation;
}