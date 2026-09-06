package com.rental.repository;

import com.rental.entity.PropertyImage;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyImageRepository extends BaseRepository<PropertyImage, Long> {

    // ================================================================
    // FIND BY PROPERTY - ✅ FIXED with @Query
    // ================================================================

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId")
    List<PropertyImage> findByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId")
    Page<PropertyImage> findByPropertyId(@Param("propertyId") Long propertyId, Pageable pageable);

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId AND pi.isActive = true")
    List<PropertyImage> findByPropertyIdAndIsActiveTrue(@Param("propertyId") Long propertyId);

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId ORDER BY pi.displayOrder ASC")
    List<PropertyImage> findByPropertyIdOrderByDisplayOrderAsc(@Param("propertyId") Long propertyId);

    // ================================================================
    // FIND PRIMARY IMAGE - ✅ FIXED with @Query
    // ================================================================

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId AND pi.isPrimary = true")
    Optional<PropertyImage> findByPropertyIdAndIsPrimaryTrue(@Param("propertyId") Long propertyId);

    List<PropertyImage> findByIsPrimaryTrue();

    // ================================================================
    // FIND BY IMAGE URL
    // ================================================================

    Optional<PropertyImage> findByImageUrl(String imageUrl);

    boolean existsByImageUrl(String imageUrl);

    // ================================================================
    // FIND ACTIVE IMAGES
    // ================================================================

    List<PropertyImage> findAllByIsActiveTrue();

    Page<PropertyImage> findAllByIsActiveTrue(Pageable pageable);

    // ================================================================
    // BULK OPERATIONS
    // ================================================================

    @Modifying
    @Transactional
    @Query("UPDATE PropertyImage pi SET pi.isPrimary = false WHERE pi.property.id = :propertyId")
    void resetPrimaryImageForProperty(@Param("propertyId") Long propertyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PropertyImage pi WHERE pi.property.id = :propertyId")
    void deleteByPropertyId(@Param("propertyId") Long propertyId);

    @Modifying
    @Transactional
    @Query("UPDATE PropertyImage pi SET pi.isActive = false WHERE pi.property.id = :propertyId")
    void deactivateByPropertyId(@Param("propertyId") Long propertyId);

    // ================================================================
    // COUNT METHODS - ✅ FIXED with @Query
    // ================================================================

    @Query("SELECT COUNT(pi) FROM PropertyImage pi WHERE pi.property.id = :propertyId")
    long countByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT COUNT(pi) FROM PropertyImage pi WHERE pi.property.id = :propertyId AND pi.isActive = true")
    long countActiveByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT COUNT(pi) FROM PropertyImage pi WHERE pi.property.id = :propertyId AND pi.isPrimary = true")
    long countPrimaryByPropertyId(@Param("propertyId") Long propertyId);

    // ================================================================
    // STATISTICS
    // ================================================================

    @Query("SELECT COUNT(pi) FROM PropertyImage pi WHERE pi.isActive = true")
    long countActiveImages();

    @Query("SELECT COUNT(pi) FROM PropertyImage pi WHERE pi.isPrimary = true")
    long countPrimaryImages();

    // ================================================================
    // FIND IMAGES WITH PAGINATION FOR GALLERY
    // ================================================================

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId AND pi.isActive = true ORDER BY pi.isPrimary DESC, pi.displayOrder ASC")
    List<PropertyImage> findGalleryImages(@Param("propertyId") Long propertyId);

    // ================================================================
    // FIND MULTIPLE PROPERTIES IMAGES (For batch loading)
    // ================================================================

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id IN :propertyIds AND pi.isActive = true ORDER BY pi.isPrimary DESC, pi.displayOrder ASC")
    List<PropertyImage> findByPropertyIds(@Param("propertyIds") List<Long> propertyIds);

    // ================================================================
    // FIND PRIMARY IMAGES FOR MULTIPLE PROPERTIES
    // ================================================================

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id IN :propertyIds AND pi.isPrimary = true AND pi.isActive = true")
    List<PropertyImage> findPrimaryImagesByPropertyIds(@Param("propertyIds") List<Long> propertyIds);
}