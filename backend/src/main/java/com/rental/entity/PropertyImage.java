package com.rental.entity;

import com.rental.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "property_images")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PropertyImage extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    private Integer displayOrder = 0;  // ✅ Added missing field

    private String caption;

    // ================================================================
    // HELPER METHODS
    // ================================================================

    public void setAsPrimary() {
        this.isPrimary = true;
    }

    public void setAsNonPrimary() {
        this.isPrimary = false;
    }

    public boolean isPrimary() {
        return isPrimary != null && isPrimary;
    }

    public String getPropertyTitle() {
        return property != null ? property.getTitle() : null;
    }

    public Long getPropertyId() {
        return property != null ? property.getId() : null;
    }

    public String getFileExtension() {
        if (imageUrl == null) return null;
        int lastDot = imageUrl.lastIndexOf('.');
        if (lastDot > 0 && lastDot < imageUrl.length() - 1) {
            return imageUrl.substring(lastDot + 1).toLowerCase();
        }
        return null;
    }

    public String getFileName() {
        if (imageUrl == null) return null;
        int lastSlash = imageUrl.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < imageUrl.length() - 1) {
            return imageUrl.substring(lastSlash + 1);
        }
        return imageUrl;
    }

    @Override
    public String toString() {
        return String.format("PropertyImage{id=%d, propertyId=%d, isPrimary=%s, displayOrder=%d}", 
            getId(), getPropertyId(), isPrimary, displayOrder);
    }
}