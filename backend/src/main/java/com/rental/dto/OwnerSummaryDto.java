package com.rental.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.rental.enums.UserRole;
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
    private UserRole role;
    @JsonProperty("isActive")
    private boolean isActive;
    private LocalDateTime createdAt;
    private Integer listingCount;  // ✅ Added
}