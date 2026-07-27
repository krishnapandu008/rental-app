package com.rental.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rental.entity.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByAvailableTrue();

    List<Property> findByOwnerId(Long ownerId);

    List<Property> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT p FROM Property p WHERE p.isActive = true AND " +
           "((p.visibility = 'PUBLIC') OR (:ownerId IS NOT NULL AND p.ownerId = :ownerId) OR (:role IS NOT NULL AND :role = 'ADMIN')) AND " +
           "(:location IS NULL OR p.location ILIKE CONCAT('%', :location, '%')) AND " +
           "(:minPrice IS NULL OR p.rent >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.rent <= :maxPrice) AND " +
           "(:bedrooms IS NULL OR p.bedrooms = :bedrooms)")
    Page<Property> findVisibleWithFilters(@Param("ownerId") Long ownerId,
                                         @Param("role") String role,
                                         @Param("location") String location,
                                         @Param("minPrice") Double minPrice,
                                         @Param("maxPrice") Double maxPrice,
                                         @Param("bedrooms") Integer bedrooms,
                                         Pageable pageable);

    @Query("SELECT p FROM Property p JOIN p.imageUrls url WHERE url = :imageUrl")
    Optional<Property> findByImageUrl(@Param("imageUrl") String imageUrl);
}