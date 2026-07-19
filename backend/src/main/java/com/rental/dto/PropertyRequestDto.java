package com.rental.dto;

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
    @NotBlank private String location;
    @NotNull @Positive private Double rent;
    @Min(1) private Integer bedrooms;
    @NotBlank private String contactNumber;
    private Boolean available;
    private Visibility visibility;        // optional, default PUBLIC in service
}