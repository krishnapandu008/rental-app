package com.rental.entity;

import com.rental.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Location extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    @Column(unique = true, nullable = false, length = 50)
    private String locationId = "";

    @Builder.Default
    @Column(nullable = false, length = 100)
    private String displayName = "";

    @Builder.Default
    @Column(length = 100)
    private String district = "";

    @Builder.Default
    @Column(length = 100)
    private String state = "";

    @Builder.Default
    @Column(length = 100)
    private String country = "";

    @Builder.Default
    @Column(length = 10)
    private String pinCode = "";

    @Builder.Default
    private Double latitude = null;

    @Builder.Default
    private Double longitude = null;

    // Added missing fields
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    // ================================================================
    // RELATIONSHIPS
    // ================================================================

    @OneToMany(mappedBy = "location")
    @Builder.Default
    private List<Property> properties = new ArrayList<>();

    @OneToMany(mappedBy = "location")
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    // ================================================================
    // HELPER METHODS
    // ================================================================

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (displayName != null) sb.append(displayName);
        if (district != null) sb.append(", ").append(district);
        if (state != null) sb.append(", ").append(state);
        if (country != null) sb.append(", ").append(country);
        if (pinCode != null) sb.append(" - ").append(pinCode);
        return sb.toString();
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    // ✅ Use getIsDefault() or check the field directly
    public boolean isDefault() {
        return Boolean.TRUE.equals(isDefault);
    }

    // ✅ Use setIsDefault() or set the field directly
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    // ✅ Convenience method to get default status as Boolean
    public Boolean getDefaultStatus() {
        return isDefault;
    }

    @Override
    public String toString() {
        return String.format("Location{id=%d, displayName='%s', state='%s', country='%s', isDefault=%s}", 
            getId(), displayName, state, country, isDefault);
    }
}