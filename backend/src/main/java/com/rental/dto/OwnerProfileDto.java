package com.rental.dto;

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
    private String role;
    private String avatarUrl;
    private LocalDateTime createdAt;
}