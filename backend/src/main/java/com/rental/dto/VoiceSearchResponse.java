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
public class VoiceSearchResponse {
    private String transcript;
    private String explanation;
    private SearchFilters filters;
    private List<PropertyResponseDto> properties;
    private long totalResults;
    private boolean aiAvailable;
}