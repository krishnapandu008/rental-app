package com.rental.repository;

import com.rental.entity.PropertyType;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyTypeRepository extends BaseRepository<PropertyType, Long> {

    // ================================================================
    // FIND BY NAME
    // ================================================================

    Optional<PropertyType> findByTypeName(String typeName);

    Optional<PropertyType> findByTypeNameAndIsActiveTrue(String typeName);

    List<PropertyType> findByTypeNameContainingIgnoreCase(String typeName);

    boolean existsByTypeName(String typeName);

    // ================================================================
    // FIND ACTIVE PROPERTY TYPES
    // ================================================================

    List<PropertyType> findAllByIsActiveTrueOrderByTypeNameAsc();

    Page<PropertyType> findAllByIsActiveTrue(Pageable pageable);

    // ================================================================
    // FIND BY ICON
    // ================================================================

    List<PropertyType> findByIcon(String icon);

    // ================================================================
    // SEARCH PROPERTY TYPES
    // ================================================================

    @Query("SELECT pt FROM PropertyType pt WHERE " +
           "(:search IS NULL OR LOWER(pt.typeName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:isActive IS NULL OR pt.isActive = :isActive)")
    Page<PropertyType> searchPropertyTypes(@Param("search") String search,
                                           @Param("isActive") Boolean isActive,
                                           Pageable pageable);

    // ================================================================
    // SUGGESTIONS / AUTOCOMPLETE
    // ================================================================

    @Query("SELECT pt.typeName FROM PropertyType pt WHERE LOWER(pt.typeName) LIKE LOWER(CONCAT(:query, '%')) AND pt.isActive = true ORDER BY pt.typeName")
    List<String> findPropertyTypeSuggestions(@Param("query") String query);

    // ================================================================
    // FIND BY IDS (Bulk operations)
    // ================================================================

    List<PropertyType> findByIdIn(List<Long> ids);

    List<PropertyType> findByIdInAndIsActiveTrue(List<Long> ids);

    // ================================================================
    // STATISTICS
    // ================================================================

    @Query("SELECT COUNT(pt) FROM PropertyType pt WHERE pt.isActive = true")
    long countActivePropertyTypes();

    // ================================================================
    // DEFAULT TYPE (Optional - if you want a default property type)
    // ================================================================

    @Query("SELECT pt FROM PropertyType pt WHERE pt.isActive = true ORDER BY pt.id LIMIT 1")
    Optional<PropertyType> findFirstActive();
}