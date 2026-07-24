package com.rental.service;

import com.rental.entity.Property;
import com.rental.enums.Visibility;
import org.springframework.stereotype.Service;

@Service
public class PropertyAccessService {

    /**
     * Check if a user (identified by ownerId and role) can view a property.
     * @param ownerId   – the authenticated owner's ID (null if unauthenticated)
     * @param role      – the authenticated owner's role (null if unauthenticated)
     * @param property  – the property to check
     * @return true if allowed
     */
    public boolean canView(Long ownerId, String role, Property property) {
        if (property == null) return false;

        // 1. Inactive → only admin can see (now includes SUPER_ADMIN)
        if (!property.isActive() && !isAdmin(role)) {
            return false;
        }

        // 2. Owner sees their own (even if private/unlisted)
        if (ownerId != null && property.getOwnerId().equals(ownerId)) {
            return true;
        }

        // 3. Admin or SUPER_ADMIN sees everything
        if (isAdmin(role)) {
            return true;
        }

        // 4. PUBLIC → everyone sees
        return property.getVisibility() == Visibility.PUBLIC;
    }

    /**
     * Check if a user can manage (edit/delete) a property.
     */
    public boolean canManage(Long ownerId, String role, Property property) {
        if (property == null || ownerId == null) return false;
        // owner or admin (includes SUPER_ADMIN)
        return property.getOwnerId().equals(ownerId) || isAdmin(role);
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