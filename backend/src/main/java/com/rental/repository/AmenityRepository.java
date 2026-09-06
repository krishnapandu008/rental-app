package com.rental.repository;

import com.rental.entity.Amenity;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmenityRepository extends BaseRepository<Amenity, Long> {

    // ================================================================
    // FIND BY NAME
    // ================================================================

    Optional<Amenity> findByAmenityName(String amenityName);

    Optional<Amenity> findByAmenityNameAndIsActiveTrue(String amenityName);

    List<Amenity> findByAmenityNameContainingIgnoreCase(String amenityName);

    boolean existsByAmenityName(String amenityName);

    // ================================================================
    // FIND BY CATEGORY
    // ================================================================

    List<Amenity> findByCategory(String category);

    List<Amenity> findByCategoryAndIsActiveTrue(String category);

    Page<Amenity> findByCategory(String category, Pageable pageable);

    // ✅ FIXED: Use @Query for distinct categories
    @Query("SELECT DISTINCT a.category FROM Amenity a WHERE a.category IS NOT NULL AND a.category != ''")
    List<String> findDistinctCategories();

    // ================================================================
    // FIND ACTIVE AMENITIES
    // ================================================================

    List<Amenity> findAllByIsActiveTrueOrderByAmenityNameAsc();

    Page<Amenity> findAllByIsActiveTrue(Pageable pageable);

    // ================================================================
    // FIND BY IDS (For bulk operations)
    // ================================================================

    List<Amenity> findByIdIn(List<Long> ids);

    List<Amenity> findByIdInAndIsActiveTrue(List<Long> ids);

    // ================================================================
    // SEARCH AMENITIES
    // ================================================================

    @Query("SELECT a FROM Amenity a WHERE " +
           "(:search IS NULL OR LOWER(a.amenityName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:isActive IS NULL OR a.isActive = :isActive)")
    Page<Amenity> searchAmenities(@Param("search") String search,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);

    // ================================================================
    // SUGGESTIONS / AUTOCOMPLETE
    // ================================================================

    @Query("SELECT a.amenityName FROM Amenity a WHERE LOWER(a.amenityName) LIKE LOWER(CONCAT(:query, '%')) AND a.isActive = true ORDER BY a.amenityName")
    List<String> findAmenitySuggestions(@Param("query") String query);

    @Query("SELECT a.amenityName FROM Amenity a WHERE LOWER(a.amenityName) LIKE LOWER(CONCAT('%', :query, '%')) AND a.isActive = true ORDER BY a.amenityName")
    List<String> findAmenitySuggestionsContaining(@Param("query") String query);

    // ================================================================
    // BULK OPERATIONS
    // ================================================================

    @Query("UPDATE Amenity a SET a.isActive = false WHERE a.id IN :ids")
    void deactivateAllByIds(@Param("ids") List<Long> ids);

    // ================================================================
    // STATISTICS
    // ================================================================

    @Query("SELECT COUNT(a) FROM Amenity a WHERE a.isActive = true")
    long countActiveAmenities();

    @Query("SELECT COUNT(a) FROM Amenity a WHERE a.category = :category")
    long countByCategory(@Param("category") String category);

    @Query("SELECT a.category, COUNT(a) FROM Amenity a GROUP BY a.category")
    List<Object[]> countAmenitiesByCategory();
}