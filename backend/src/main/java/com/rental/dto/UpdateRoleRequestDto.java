package com.rental.dto;

import com.rental.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequestDto {
    @NotBlank
    private UserRole role;
}