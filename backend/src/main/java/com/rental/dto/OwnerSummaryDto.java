package com.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerSummaryDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private boolean isActive;          // ✅ added
    private LocalDateTime createdAt;   // ✅ added
}