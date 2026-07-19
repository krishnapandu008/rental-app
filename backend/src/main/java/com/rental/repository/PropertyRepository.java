package com.rental.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rental.entity.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByAvailableTrue();

    List<Property> findByOwnerId(Long ownerId);

    List<Property> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT p FROM Property p WHERE p.isActive = true AND " +
           "(p.visibility = 'PUBLIC' OR :ownerId IS NOT NULL AND p.ownerId = :ownerId OR :role = 'ADMIN')")
    List<Property> findVisibleForUser(@Param("ownerId") Long ownerId, @Param("role") String role);

    // ✅ FIXED: Use JOIN instead of MEMBER OF for better compatibility
    @Query("SELECT p FROM Property p JOIN p.imageUrls url WHERE url = :imageUrl")
    Optional<Property> findByImageUrl(@Param("imageUrl") String imageUrl);
}