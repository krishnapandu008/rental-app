package com.rental.entity;

import com.rental.entity.base.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "addresses")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Address extends BaseEntity {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private String streetName;
    private String streetNumber;
    private String areaName;
    private String landmark;
    private String formattedAddress;

    // ✅ Relationship with Property (One-to-One)
    @OneToOne(mappedBy = "address")
    private Property property;

    // ✅ Helper method
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (streetNumber != null) sb.append(streetNumber).append(", ");
        if (streetName != null) sb.append(streetName).append(", ");
        if (areaName != null) sb.append(areaName).append(", ");
        if (landmark != null) sb.append(landmark).append(", ");
        if (location != null) sb.append(location.getDisplayName()).append(", ");
        if (location != null && location.getDistrict() != null) sb.append(location.getDistrict()).append(", ");
        if (location != null && location.getState() != null) sb.append(location.getState());
        return sb.toString();
    }
}