package com.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.rental.enums.UserRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String token;
    private String refreshToken;
    private UserRole role;
    private String avatarUrl;   // ✅ NEW
}