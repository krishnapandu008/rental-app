package com.rental.service;

import com.rental.entity.Property;
import com.rental.enums.Visibility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PropertyAccessService {

    /**
     * Check if a user (identified by ownerId and role) can view a property.
     * @param ownerId   – the authenticated owner's ID (null if unauthenticated)
     * @param role      – the authenticated owner's role (null if unauthenticated)
     * @param property  – the property to check
     * @return true if allowed
     */
    public boolean canView(Long ownerId, String role, Property property) {
        if (property == null) {
            log.warn("Property is null, cannot check access");
            return false;
        }

        // 1. Inactive → only admin can see
        if (!property.isActive() && !isAdmin(role)) {
            log.debug("Property {} is inactive and user is not admin", property.getId());
            return false;
        }

        // 2. Owner sees their own (even if private/unlisted)
        if (ownerId != null && isOwner(property, ownerId)) {
            log.debug("User {} is the owner of property {}", ownerId, property.getId());
            return true;
        }

        // 3. Admin sees everything
        if (isAdmin(role)) {
            log.debug("Admin user can view property {}", property.getId());
            return true;
        }

        // 4. PUBLIC → everyone sees
        boolean isPublic = property.getVisibility() == Visibility.PUBLIC;
        log.debug("Property {} visibility: {}, public access: {}", property.getId(), property.getVisibility(), isPublic);
        return isPublic;
    }

    /**
     * Check if a user can manage (edit/delete) a property.
     */
    public boolean canManage(Long ownerId, String role, Property property) {
        if (property == null) {
            log.warn("Property is null, cannot check manage access");
            return false;
        }
        
        if (ownerId == null) {
            log.debug("No ownerId provided, cannot manage property {}", property.getId());
            return false;
        }

        // Owner can manage their own property OR admin can manage any property
        boolean canManage = isOwner(property, ownerId) || isAdmin(role);
        
        log.debug("User {} can manage property {}: {}", ownerId, property.getId(), canManage);
        return canManage;
    }

    /**
     * Check if the given ownerId matches the property owner's ID.
     */
    private boolean isOwner(Property property, Long ownerId) {
        if (property.getOwner() == null) {
            log.debug("Property {} has no owner", property.getId());
            return false;
        }
        return property.getOwner().getId().equals(ownerId);
    }

    /**
     * Determines if the given role has administrative privileges.
     * Both "ADMIN" and "SUPER_ADMIN" are considered admin.
     */
    private boolean isAdmin(String role) {
        return role != null && 
               (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("SUPER_ADMIN"));
    }
}