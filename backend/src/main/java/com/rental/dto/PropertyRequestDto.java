package com.rental.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List; // if you have it, but not needed for this fix

@Data
public class PropertyRequestDto {
    @NotBlank private String title;
    private String description;
    @NotBlank private String location;
    @NotNull @Positive private Double rent;
    @Min(1) private Integer bedrooms;
    @NotBlank private String contactNumber;
    private Long ownerId;
    private Boolean available;   // <-- Add this line
}