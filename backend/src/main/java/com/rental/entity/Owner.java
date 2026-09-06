package com.rental.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rental.entity.base.BaseEntity;
import com.rental.enums.UserRole;   // ✅ Import the enum

@Entity
@Table(name = "owners")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Owner extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;

    // ✅ Changed to UserRole enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @Builder.Default
    private LocalDateTime lastLoginAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "avatar_url")
    private String avatarUrl = "";

    @Builder.Default
    @Column(name = "joined_date")
    private LocalDateTime joinedDate = LocalDateTime.now();

    /* @Builder.Default
    @Column(name = "listing_count")
    private Integer listingCount = 0; */

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    @Column(name = "response_rate")
    private Double responseRate = 0.0;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Property> properties = new ArrayList<>();

    // ================================================================
    // HELPER METHODS
    // ================================================================

    /* public void addProperty(Property property) {
        properties.add(property);
        property.setOwner(this);
        listingCount = properties.size();
    } */

    /* public void removeProperty(Property property) {
        properties.remove(property);
        property.setOwner(null);
        listingCount = properties.size();
    } */

    public boolean isActive() {
        return isActive != null && isActive;
    }

    public boolean isLocked() {
        return isLocked != null && isLocked;
    }

    // ✅ Updated to use enum
    public boolean isOwner() {
        return role == UserRole.OWNER || role == UserRole.ADMIN; // ADMIN is also considered owner for some permissions
    }

    public boolean isAdmin() {
    	return role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN;
    }

    @Override
    public String toString() {
        return String.format("Owner{id=%d, email='%s', name='%s', role='%s'}", 
            getId(), email, name, role);
    }
}