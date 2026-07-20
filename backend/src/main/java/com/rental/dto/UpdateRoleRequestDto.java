package com.rental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequestDto {
    @NotBlank
    private String role;
}