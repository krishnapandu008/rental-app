package com.rental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "owners")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;
    private String phone;

    @Column(nullable = false, columnDefinition = "varchar(255) default 'USER'")
    private String role;   // USER, ADMIN, SUPER_ADMIN

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isLocked = false;

    @Column(nullable = true)
    private String avatarUrl;   // ✅ NEW

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;
}