package com.rental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InquiryReplyDto {
    @NotBlank
    private String reply;
}