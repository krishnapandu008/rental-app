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
           "(:location IS NULL OR p.location ILIKE CONCAT('%', CAST(:location AS text), '%')) AND " +
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
    
    /**
     * Find properties within a radius (in meters) of a given location
     * ✅ Uses single-line string concatenation (no text blocks)
     */
    @Query(value = "SELECT p.*, " +
           "ST_Distance(" +
           "ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, " +
           "ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography" +
           ") AS distance " +
           "FROM properties p " +
           "WHERE ST_DWithin(" +
           "ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, " +
           "ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography, " +
           ":radiusInMeters" +
           ") " +
           "AND p.available = true " +
           "AND p.is_active = true " +
           "AND p.latitude IS NOT NULL " +
           "AND p.longitude IS NOT NULL " +
           "ORDER BY distance", nativeQuery = true)
    List<Object[]> findNearbyProperties(
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("radiusInMeters") Double radiusInMeters
    );

    /**
     * Find properties within a radius with additional filters
     * ✅ Uses single-line string concatenation (no text blocks)
     */
    @Query(value = "SELECT p.*, " +
           "ST_Distance(" +
           "ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, " +
           "ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography" +
           ") AS distance " +
           "FROM properties p " +
           "WHERE ST_DWithin(" +
           "ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, " +
           "ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography, " +
           ":radiusInMeters" +
           ") " +
           "AND (:minRent IS NULL OR p.rent >= :minRent) " +
           "AND (:maxRent IS NULL OR p.rent <= :maxRent) " +
           "AND (:bedrooms IS NULL OR p.bedrooms = :bedrooms) " +
           "AND p.available = true " +
           "AND p.is_active = true " +
           "AND p.latitude IS NOT NULL " +
           "AND p.longitude IS NOT NULL " +
           "ORDER BY distance", nativeQuery = true)
    List<Object[]> findNearbyPropertiesWithFilters(
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("radiusInMeters") Double radiusInMeters,
        @Param("minRent") Double minRent,
        @Param("maxRent") Double maxRent,
        @Param("bedrooms") Integer bedrooms
    );
    
    /**
     * Get all properties that have latitude and longitude (for map display)
     */
    @Query("SELECT p FROM Property p WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL AND p.available = true AND p.isActive = true")
    List<Property> findAllWithCoordinates();
}