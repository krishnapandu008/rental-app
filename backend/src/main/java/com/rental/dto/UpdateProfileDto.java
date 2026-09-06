package com.rental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileDto {
    @NotBlank @Email private String email;
    @NotBlank private String name;
    @NotBlank private String phone;
    private String avatarUrl; // ✅ Added for profile picture
}