package com.rental.dto;

import com.rental.enums.UserRole;  // ✅ Import enum
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerRegisterDto {
    @NotBlank @Email private String email;
    @NotBlank private String password;
    @NotBlank private String name;
    @NotBlank private String phone;
    private UserRole role; // ✅ Changed to enum (optional, defaults to USER in service)
}