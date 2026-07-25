package com.rental.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isActive") // ✅ force JSON key to "isActive"
    private boolean isActive;
    private LocalDateTime createdAt;
}