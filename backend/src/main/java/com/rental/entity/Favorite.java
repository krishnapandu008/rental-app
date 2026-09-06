package com.rental.entity;

import com.rental.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"owner_id", "property_id"})  // ✅ Changed from user_id
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Favorite extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    @Column(name = "owner_id", nullable = false)
    private Long ownerId = 0L;

    @Builder.Default
    @Column(name = "property_id", nullable = false)
    private Long propertyId = 0L;

    @Builder.Default
    private LocalDateTime favoritedAt = LocalDateTime.now();

    // ✅ Relationship with Owner (not User)
    @ManyToOne
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private Owner owner;

    // Relationship with Property
    @ManyToOne
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    // ================================================================
    // HELPER METHODS
    // ================================================================

    public boolean isValid() {
        return ownerId != null && propertyId != null;
    }

    public String getPropertyTitle() {
        return property != null ? property.getTitle() : null;
    }

    public String getOwnerName() {
        return owner != null ? owner.getName() : null;
    }

    public String getOwnerEmail() {
        return owner != null ? owner.getEmail() : null;
    }

    public Double getPropertyRent() {
        return property != null ? property.getRent() : null;
    }

    public String getPropertyLocation() {
        return property != null ? property.getLocationDisplayName() : null;
    }

    @Override
    public String toString() {
        return String.format("Favorite{ownerId=%d, propertyId=%d, favoritedAt=%s}", 
            ownerId, propertyId, favoritedAt);
    }
}