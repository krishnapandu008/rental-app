package com.rental.repository;

import com.rental.entity.Property;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends BaseRepository<Property, Long> {

    // ================================================================
    // FIND BY OWNER - ✅ FIXED with explicit @Query
    // ================================================================

    @Query("SELECT p FROM Property p WHERE p.owner.id = :ownerId")
    List<Property> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT p FROM Property p WHERE p.owner.id = :ownerId")
    Page<Property> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.owner.id = :ownerId AND p.isActive = true")
    List<Property> findByOwnerIdAndIsActiveTrue(@Param("ownerId") Long ownerId);

    // ================================================================
    // FIND BY LOCATION - ✅ FIXED with explicit @Query
    // ================================================================

    @Query("SELECT p FROM Property p WHERE p.location.id = :locationId")
    List<Property> findByLocationId(@Param("locationId") Long locationId);

    @Query("SELECT p FROM Property p WHERE p.location.id = :locationId")
    Page<Property> findByLocationId(@Param("locationId") Long locationId, Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.location.id = :locationId AND p.isActive = true")
    List<Property> findByLocationIdAndIsActiveTrue(@Param("locationId") Long locationId);

    // ================================================================
    // FIND BY AVAILABILITY
    // ================================================================

    List<Property> findByIsAvailableTrue();

    List<Property> findByIsAvailableTrueAndIsActiveTrue();

    Page<Property> findByIsAvailableTrue(Pageable pageable);

    // ================================================================
    // FIND BY VISIBILITY
    // ================================================================

    List<Property> findByVisibility(com.rental.enums.Visibility visibility);

    Page<Property> findByVisibility(com.rental.enums.Visibility visibility, Pageable pageable);

    // ================================================================
    // FIND WITH FILTERS
    // ================================================================

    @Query("SELECT DISTINCT p FROM Property p " +
    	       "LEFT JOIN FETCH p.location " +
    	       "LEFT JOIN FETCH p.address " +
    	       "LEFT JOIN FETCH p.propertyType " +
    	       "LEFT JOIN FETCH p.owner " +
    	       "WHERE p.isActive = true AND " +
    	       "((p.visibility = 'PUBLIC') OR (:ownerId IS NOT NULL AND p.owner.id = :ownerId) OR (:role IS NOT NULL AND :role = 'ADMIN')) AND " +
    	       "(:locationId IS NULL OR p.location.id = :locationId) AND " +
    	       "(:minPrice IS NULL OR p.rent >= :minPrice) AND " +
    	       "(:maxPrice IS NULL OR p.rent <= :maxPrice) AND " +
    	       "(:bedrooms IS NULL OR p.bedrooms = :bedrooms) AND " +
    	       "(:amenities IS NULL OR EXISTS (SELECT a FROM p.amenities a WHERE a.id IN :amenities))")
    	Page<Property> findVisibleWithFilters(@Param("ownerId") Long ownerId,
    	                                      @Param("role") String role,
    	                                      @Param("locationId") Long locationId,
    	                                      @Param("minPrice") Double minPrice,
    	                                      @Param("maxPrice") Double maxPrice,
    	                                      @Param("bedrooms") Integer bedrooms,
    	                                      @Param("amenities") List<Long> amenities,
    	                                      Pageable pageable);

    // ================================================================
    // FIND BY LOCATION NAME (String for backward compatibility)
    // ================================================================

    @Query("SELECT p FROM Property p WHERE p.isActive = true AND LOWER(p.locationName) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Property> findByLocationNameContainingIgnoreCase(@Param("location") String location);

    // ================================================================
    // FIND BY IMAGE URL
    // ================================================================

    @Query("SELECT p FROM Property p JOIN p.images img WHERE img.imageUrl = :imageUrl")
    Optional<Property> findByImageUrl(@Param("imageUrl") String imageUrl);

    // ================================================================
    // FIND NEARBY PROPERTIES (Spatial queries)
    // ================================================================

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
           "AND p.is_available = true " +
           "AND p.is_active = true " +
           "AND p.latitude IS NOT NULL " +
           "AND p.longitude IS NOT NULL " +
           "ORDER BY distance", nativeQuery = true)
    List<Object[]> findNearbyProperties(
        @Param("lat") Double lat,
        @Param("lng") Double lng,
        @Param("radiusInMeters") Double radiusInMeters
    );

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
           "AND p.is_available = true " +
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

    // ================================================================
    // MAP VIEW
    // ================================================================

    @Query("SELECT p FROM Property p WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL AND p.isAvailable = true AND p.isActive = true")
    List<Property> findAllWithCoordinates();

    // ================================================================
    // SEARCH PROPERTIES (Advanced)
    // ================================================================

    @Query("SELECT p FROM Property p WHERE p.isActive = true AND " +
           "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:locationId IS NULL OR p.location.id = :locationId) AND " +
           "(:minRent IS NULL OR p.rent >= :minRent) AND " +
           "(:maxRent IS NULL OR p.rent <= :maxRent) AND " +
           "(:bedrooms IS NULL OR p.bedrooms = :bedrooms)")
    Page<Property> searchProperties(@Param("search") String search,
                                    @Param("locationId") Long locationId,
                                    @Param("minRent") Double minRent,
                                    @Param("maxRent") Double maxRent,
                                    @Param("bedrooms") Integer bedrooms,
                                    Pageable pageable);

    // ================================================================
    // STATISTICS
    // ================================================================

    @Query("SELECT COUNT(p) FROM Property p WHERE p.isActive = true")
    long countActiveProperties();

    @Query("SELECT COUNT(p) FROM Property p WHERE p.isAvailable = true AND p.isActive = true")
    long countAvailableProperties();

    @Query("SELECT COUNT(p) FROM Property p WHERE p.owner.id = :ownerId")
    long countByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT AVG(p.rent) FROM Property p WHERE p.isActive = true AND p.isAvailable = true")
    Double getAverageRent();
 // ================================================================
 // COUNT ACTIVE PROPERTIES FOR OWNER
 // ================================================================

 @Query("SELECT COUNT(p) FROM Property p WHERE p.owner.id = :ownerId AND p.isActive = true")
 long countActivePropertiesByOwnerId(@Param("ownerId") Long ownerId);
}