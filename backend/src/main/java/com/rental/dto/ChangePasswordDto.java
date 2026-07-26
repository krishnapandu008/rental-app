package com.rental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotBlank private String oldPassword;
    @NotBlank private String newPassword;
}