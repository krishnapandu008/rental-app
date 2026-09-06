package com.rental.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.rental.entity.base.BaseEntity;
import com.rental.enums.Visibility;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "properties")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Property extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

    // ================================================================
    // RELATIONSHIPS
    // ================================================================

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne
    @JoinColumn(name = "property_type_id")
    private PropertyType propertyType;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    // ================================================================
    // CORE FIELDS
    // ================================================================

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Keep for backward compatibility during migration
    @Builder.Default
    private String locationName = "";

    @Builder.Default
    @Column(nullable = false)
    private Double rent = 0.0;

    @Builder.Default
    private Integer bedrooms = 0;

    @Builder.Default
    private Double bathrooms = 0.0;

    @Builder.Default
    private Integer squareFeet = 0;

    @Builder.Default
    @Column(length = 20)
    private String contactNumber = "";
    
    @Builder.Default
    @Column(name = "available", nullable = false)
    private Boolean isAvailable = true;

    @Builder.Default
    private Double latitude = null;

    @Builder.Default
    private Double longitude = null;

    // ================================================================
    // COLLECTIONS
    // ================================================================

    @ManyToMany
    @JoinTable(
        name = "property_amenity",
        joinColumns = @JoinColumn(name = "property_id"),
        inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @Builder.Default
    private List<Amenity> amenities = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyImage> images = new ArrayList<>();

    // ================================================================
    // ENUMS
    // ================================================================
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    // ================================================================
    // IMAGE MANAGEMENT
    // ================================================================

    public void addImage(PropertyImage image) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(image);
        image.setProperty(this);
    }

    public void removeImage(PropertyImage image) {
        if (images != null) {
            images.remove(image);
            image.setProperty(null);
        }
    }

    public void removeImageByUrl(String imageUrl) {
        if (images != null && imageUrl != null) {
            images.removeIf(img -> imageUrl.equals(img.getImageUrl()));
        }
    }

    public void setPrimaryImage(PropertyImage primaryImage) {
        if (images != null && primaryImage != null && images.contains(primaryImage)) {
            images.forEach(img -> img.setIsPrimary(false));
            primaryImage.setIsPrimary(true);
        }
    }

    public String getPrimaryImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.stream()
                .filter(PropertyImage::getIsPrimary)
                .findFirst()
                .map(PropertyImage::getImageUrl)
                .orElse(images.get(0).getImageUrl());
        }
        return null;
    }

    public List<String> getAllImageUrls() {
        if (images != null && !images.isEmpty()) {
            return images.stream()
                .map(PropertyImage::getImageUrl)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public int getImageCount() {
        return images != null ? images.size() : 0;
    }

    // ================================================================
    // AMENITY MANAGEMENT
    // ================================================================

    public void addAmenity(Amenity amenity) {
        if (amenities == null) {
            amenities = new ArrayList<>();
        }
        if (!amenities.contains(amenity)) {
            amenities.add(amenity);
            if (amenity.getProperties() != null && !amenity.getProperties().contains(this)) {
                amenity.getProperties().add(this);
            }
        }
    }

    public void addAllAmenities(List<Amenity> amenityList) {
        if (amenityList != null) {
            for (Amenity amenity : amenityList) {
                addAmenity(amenity);
            }
        }
    }

    public void removeAmenity(Amenity amenity) {
        if (amenities != null) {
            amenities.remove(amenity);
            if (amenity.getProperties() != null) {
                amenity.getProperties().remove(this);
            }
        }
    }

    public boolean hasAmenity(Amenity amenity) {
        return amenities != null && amenity != null && amenities.contains(amenity);
    }

    public boolean hasAllAmenities(List<Amenity> requiredAmenities) {
        if (requiredAmenities == null || requiredAmenities.isEmpty()) {
            return true;
        }
        if (amenities == null || amenities.isEmpty()) {
            return false;
        }
        return amenities.containsAll(requiredAmenities);
    }

    public boolean hasAmenities() {
        return amenities != null && !amenities.isEmpty();
    }

    public int getAmenityCount() {
        return amenities != null ? amenities.size() : 0;
    }

    public List<Long> getAmenityIds() {
        if (amenities != null && !amenities.isEmpty()) {
            return amenities.stream()
                .map(Amenity::getId)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    // ================================================================
    // LOCATION & ADDRESS
    // ================================================================

    public String getFullLocation() {
        if (location != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(location.getDisplayName());
            if (location.getDistrict() != null) {
                sb.append(", ").append(location.getDistrict());
            }
            if (location.getState() != null) {
                sb.append(", ").append(location.getState());
            }
            if (location.getPinCode() != null) {
                sb.append(" - ").append(location.getPinCode());
            }
            return sb.toString();
        }
        return locationName != null ? locationName : "Location not set";
    }

    public String getLocationDisplayName() {
        if (location != null) {
            return location.getDisplayName();
        }
        return locationName;
    }

    public Long getLocationId() {
        return location != null ? location.getId() : null;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public String getFormattedAddress() {
        if (address != null) {
            return address.getFullAddress();
        }
        return getFullLocation();
    }

    public boolean hasAddress() {
        return address != null;
    }

    // ================================================================
    // OWNER INFO
    // ================================================================

    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }

    public String getOwnerName() {
        return owner != null ? owner.getName() : null;
    }

    public String getOwnerEmail() {
        return owner != null ? owner.getEmail() : null;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    // ================================================================
    // PROPERTY TYPE
    // ================================================================

    public String getPropertyTypeName() {
        return propertyType != null ? propertyType.getTypeName() : null;
    }

    public Long getPropertyTypeId() {
        return propertyType != null ? propertyType.getId() : null;
    }

    public boolean hasPropertyType() {
        return propertyType != null;
    }

    // ================================================================
    // STATUS CHECKS
    // ================================================================

    public boolean isAvailableForRent() {
        return isActive() && isAvailable != null && isAvailable;
    }

    public boolean isPublic() {
        return visibility == Visibility.PUBLIC;
    }

    public boolean isListed() {
        return isActive() && isAvailable != null && isAvailable && visibility == Visibility.PUBLIC;
    }

    public boolean isVisibleToPublic() {
        return isActive() && visibility == Visibility.PUBLIC;
    }

    // ================================================================
    // TO STRING
    // ================================================================

    @Override
    public String toString() {
        return String.format("Property{id=%d, title='%s', rent=%.2f, location='%s', available=%s}", 
            getId(), title, rent, getLocationDisplayName(), isAvailable);
    }
}