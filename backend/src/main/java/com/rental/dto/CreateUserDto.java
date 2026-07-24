package com.rental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserDto {
    @NotBlank @Email private String email;
    @NotBlank private String password;
    @NotBlank private String name;
    @NotBlank private String phone;
    private String role; // optional, default USER
}