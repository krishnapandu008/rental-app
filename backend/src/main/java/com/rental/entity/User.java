package com.rental.entity;

import com.rental.entity.base.BaseEntity;
import com.rental.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Builder.Default
    @Column(columnDefinition = "TEXT")
    private String avatarUrl = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Builder.Default
    private LocalDateTime lastLoginAt = LocalDateTime.now();

    // ================================================================
    // HELPER METHODS
    // ================================================================

    public boolean isOwner() {
        return role == UserRole.OWNER || role == UserRole.ADMIN;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isRenter() {
        return role == UserRole.USER;
    }

    public boolean isVerified() {
        return isVerified != null && isVerified;
    }

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, email='%s', name='%s', role=%s}", 
            getId(), email, name, role);
    }
}