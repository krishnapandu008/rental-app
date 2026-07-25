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

    // ✅ Updated query to include optional location filter
    @Query("SELECT p FROM Property p WHERE p.isActive = true AND " +
           "(p.visibility = 'PUBLIC' OR :ownerId IS NOT NULL AND p.ownerId = :ownerId OR :role = 'ADMIN') AND " +
           "(:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<Property> findVisibleForUser(@Param("ownerId") Long ownerId,
                                      @Param("role") String role,
                                      @Param("location") String location);

    // Query to find property by image URL
    @Query("SELECT p FROM Property p JOIN p.imageUrls url WHERE url = :imageUrl")
    Optional<Property> findByImageUrl(@Param("imageUrl") String imageUrl);
}