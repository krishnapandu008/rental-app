package com.rental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InquiryRequestDto {
    @NotBlank
    private String message;
}