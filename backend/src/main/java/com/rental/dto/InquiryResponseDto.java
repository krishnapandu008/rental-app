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
    private Long senderId;
    private String senderEmail;
    private String message;
    private String reply;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;
    private Boolean isReceived;  // ✅ NEW: true if user is the property owner
    private Boolean isSent;      // ✅ NEW: true if user is the sender
}