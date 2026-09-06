package com.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDto {
    private Long id;
    private String streetName;
    private String streetNumber;
    private String areaName;
    private String landmark;
    private String formattedAddress;
    private LocationResponseDto location;
}