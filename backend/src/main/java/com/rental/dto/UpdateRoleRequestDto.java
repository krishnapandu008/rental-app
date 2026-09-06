package com.rental.dto;

import com.rental.enums.UserRole;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequestDto {
    @NotNull
    private UserRole role;
}