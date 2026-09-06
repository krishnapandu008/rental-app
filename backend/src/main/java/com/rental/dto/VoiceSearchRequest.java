package com.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceSearchRequest {
	@NotBlank(message = "Query cannot be empty")
    @Size(max = 500, message = "Query cannot exceed 500 characters")
    private String query;
}