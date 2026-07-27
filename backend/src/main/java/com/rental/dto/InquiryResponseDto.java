package com.rental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InquiryResponseDto {
    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private String senderName;
    private String senderEmail;
    private String message;
    private String reply;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;
}