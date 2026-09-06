package com.rental.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.rental.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "audit_logs")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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