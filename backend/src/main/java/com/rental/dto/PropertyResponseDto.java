package com.rental.dto;

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
}