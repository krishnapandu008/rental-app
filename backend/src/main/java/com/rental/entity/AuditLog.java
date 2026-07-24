package com.rental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;
    private String adminEmail;
    private String action;          // e.g., "DELETE_USER", "UPDATE_ROLE"
    @Column(columnDefinition = "TEXT")
    private String details;         // JSON payload or description
    private String ipAddress;

    @CreationTimestamp
    private LocalDateTime timestamp;
}