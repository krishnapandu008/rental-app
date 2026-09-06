package com.rental.repository;

import com.rental.entity.Location;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends BaseRepository<Location, Long> {

    // ================================================================
    // FIND BY IDENTIFIER
    // ================================================================

    Optional<Location> findByLocationId(String locationId);

    Optional<Location> findByLocationIdAndIsActiveTrue(String locationId);

    // ================================================================
    // DEFAULT LOCATION
    // ================================================================

    Optional<Location> findByIsDefaultTrue();

    Optional<Location> findByIsDefaultTrueAndIsActiveTrue();

    // ================================================================
    // FIND ACTIVE LOCATIONS
    // ================================================================

    // ✅ FIXED: Changed 'NameAsc' to 'DisplayNameAsc'
    List<Location> findAllByIsActiveTrueOrderByDisplayOrderAscDisplayNameAsc();

    List<Location> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    Page<Location> findAllByIsActiveTrue(Pageable pageable);

    // ================================================================
    // SEARCH LOCATIONS
    // ================================================================

    List<Location> findByDisplayNameContainingIgnoreCase(String displayName);

    Page<Location> findByDisplayNameContainingIgnoreCase(String displayName, Pageable pageable);

    List<Location> findByDistrictContainingIgnoreCase(String district);

    List<Location> findByStateContainingIgnoreCase(String state);

    // ================================================================
    // ADVANCED SEARCH
    // ================================================================

    @Query("SELECT l FROM Location l WHERE " +
           "(:search IS NULL OR LOWER(l.displayName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.district) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.state) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:isActive IS NULL OR l.isActive = :isActive)")
    Page<Location> searchLocations(@Param("search") String search,
                                   @Param("isActive") Boolean isActive,
                                   Pageable pageable);

    // ================================================================
    // COUNTRY / STATE / DISTRICT
    // ================================================================

    List<Location> findByCountry(String country);

    List<Location> findByState(String state);

    List<Location> findByDistrict(String district);

    List<Location> findByStateAndIsActiveTrue(String state);

    // ================================================================
    // SUGGESTIONS / AUTOCOMPLETE
    // ================================================================

    @Query("SELECT l.displayName FROM Location l WHERE LOWER(l.displayName) LIKE LOWER(CONCAT(:query, '%')) AND l.isActive = true ORDER BY l.displayOrder ASC, l.displayName ASC")
    List<String> findLocationSuggestions(@Param("query") String query);

    @Query("SELECT l.displayName FROM Location l WHERE LOWER(l.displayName) LIKE LOWER(CONCAT('%', :query, '%')) AND l.isActive = true ORDER BY l.displayName ASC")
    List<String> findLocationSuggestionsContaining(@Param("query") String query);

    // ================================================================
    // STATISTICS
    // ================================================================

    @Query("SELECT COUNT(l) FROM Location l WHERE l.isActive = true")
    long countActiveLocations();

    @Query("SELECT COUNT(l) FROM Location l WHERE l.isDefault = true")
    long countDefaultLocations();

    // ================================================================
    // BULK OPERATIONS
    // ================================================================

    @Query("UPDATE Location l SET l.isActive = false WHERE l.isDefault = true")
    void deactivateAllDefaultLocations();

    // ================================================================
    // FIND ALL ACTIVE (Alternative if you need the old method name)
    // ================================================================

    default List<Location> findAllByIsActiveTrueOrderByDisplayOrderAscNameAsc() {
        return findAllByIsActiveTrueOrderByDisplayOrderAscDisplayNameAsc();
    }
}