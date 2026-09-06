package com.rental.dto;
import com.rental.enums.UserRole;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OwnerProfileDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private UserRole role;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private Integer listingCount;
}