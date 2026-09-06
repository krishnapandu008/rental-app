package com.rental.entity;

import com.rental.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "renters")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Renter extends BaseEntity {
    private static final long serialVersionUID = 1L;

    // ================================================================
    // USER FIELDS (Copied from User)
    // ================================================================

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false)
    private String role = "USER";

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Builder.Default
    private LocalDateTime lastLoginAt = LocalDateTime.now();

    // ================================================================
    // RENTER SPECIFIC FIELDS
    // ================================================================

    @Column(name = "preferred_location")
    private String preferredLocation;

    @Column(name = "max_rent")
    private Double maxRent;

    @ElementCollection
    @CollectionTable(name = "renter_preferred_amenities", joinColumns = @JoinColumn(name = "renter_id"))
    @Column(name = "preferred_amenity")
    @Builder.Default
    private List<String> preferredAmenities = new ArrayList<>();

    // Saved Searches
    @ElementCollection
    @CollectionTable(name = "renter_saved_searches", joinColumns = @JoinColumn(name = "renter_id"))
    @Column(name = "search_query")
    @Builder.Default
    private List<String> savedSearches = new ArrayList<>();

    // ================================================================
    // HELPER METHODS
    // ================================================================

    public void addSavedSearch(String search) {
        if (savedSearches == null) {
            savedSearches = new ArrayList<>();
        }
        savedSearches.add(search);
    }

    public void removeSavedSearch(String search) {
        if (savedSearches != null) {
            savedSearches.remove(search);
        }
    }

    public void addPreferredAmenity(String amenity) {
        if (preferredAmenities == null) {
            preferredAmenities = new ArrayList<>();
        }
        preferredAmenities.add(amenity);
    }

    public void removePreferredAmenity(String amenity) {
        if (preferredAmenities != null) {
            preferredAmenities.remove(amenity);
        }
    }

    public boolean isVerified() {
        return isVerified != null && isVerified;
    }

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    public boolean isOwner() {
        return "OWNER".equals(role) || "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    @Override
    public String toString() {
        return String.format("Renter{id=%d, email='%s', name='%s', role='%s', preferredLocation='%s'}", 
            getId(), email, name, role, preferredLocation);
    }
}