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

    @Query(value = "SELECT * FROM properties p WHERE p.is_active = true AND " +
    	       "((p.visibility = 'PUBLIC') OR (?1 IS NOT NULL AND p.owner_id = ?1) OR (?2 IS NOT NULL AND ?2 = 'ADMIN')) AND " +
    	       "(?3 IS NULL OR p.location ILIKE CONCAT('%', ?3, '%')) AND " +
    	       "(?4 IS NULL OR p.rent >= ?4) AND " +
    	       "(?5 IS NULL OR p.rent <= ?5) AND " +
    	       "(?6 IS NULL OR p.bedrooms = ?6) ORDER BY p.created_at DESC",
    	       nativeQuery = true)
    	List<Property> findVisibleWithFiltersNative(@Param("ownerId") Long ownerId,
    	                                           @Param("role") String role,
    	                                           @Param("location") String location,
    	                                           @Param("minPrice") Double minPrice,
    	                                           @Param("maxPrice") Double maxPrice,
    	                                           @Param("bedrooms") Integer bedrooms);

    @Query("SELECT p FROM Property p JOIN p.imageUrls url WHERE url = :imageUrl")
    Optional<Property> findByImageUrl(@Param("imageUrl") String imageUrl);
}