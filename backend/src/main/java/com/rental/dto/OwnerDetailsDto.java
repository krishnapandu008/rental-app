package com.rental.dto;
import com.rental.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDetailsDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private UserRole role;
    private boolean isActive;
    private boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private Integer listingCount;
}