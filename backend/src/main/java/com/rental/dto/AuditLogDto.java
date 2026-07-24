package com.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private Long adminId;
    private String adminEmail;
    private String action;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
}