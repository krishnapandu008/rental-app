package com.rental.repository;

import com.rental.entity.Favorite;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends BaseRepository<Favorite, Long> {

    // ================================================================
    // FIND BY OWNER (FIXED: Use ownerId instead of userId)
    // ================================================================

    @Query("SELECT f FROM Favorite f WHERE f.ownerId = :ownerId")
    List<Favorite> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT f FROM Favorite f WHERE f.ownerId = :ownerId AND f.propertyId = :propertyId")
    List<Favorite> findByOwnerIdAndPropertyId(@Param("ownerId") Long ownerId, @Param("propertyId") Long propertyId);

    @Query("SELECT f FROM Favorite f WHERE f.ownerId = :ownerId AND f.isActive = true")
    List<Favorite> findByOwnerIdAndIsActiveTrue(@Param("ownerId") Long ownerId);

    // ================================================================
    // COUNT METHODS
    // ================================================================

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.ownerId = :ownerId AND f.isActive = true")
    long countByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.propertyId = :propertyId AND f.isActive = true")
    long countByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.ownerId = :ownerId")
    long countByOwnerIdTotal(@Param("ownerId") Long ownerId);

    // ================================================================
    // FIND PROPERTY IDs
    // ================================================================

    @Query("SELECT f.propertyId FROM Favorite f WHERE f.ownerId = :ownerId AND f.isActive = true")
    List<Long> findPropertyIdsByOwnerId(@Param("ownerId") Long ownerId);

    // ================================================================
    // DELETE
    // ================================================================

    @Modifying
    @Transactional
    @Query("DELETE FROM Favorite f WHERE f.ownerId = :ownerId AND f.propertyId = :propertyId")
    void deleteByOwnerIdAndPropertyId(@Param("ownerId") Long ownerId, @Param("propertyId") Long propertyId);

    // ================================================================
    // EXISTS
    // ================================================================

    @Query("SELECT COUNT(f) > 0 FROM Favorite f WHERE f.ownerId = :ownerId AND f.propertyId = :propertyId")
    boolean existsByOwnerIdAndPropertyId(@Param("ownerId") Long ownerId, @Param("propertyId") Long propertyId);

    Optional<Favorite> findByOwnerIdAndPropertyIdAndIsActiveTrue(Long ownerId, Long propertyId);

    // ================================================================
    // ORDER BY FAVORITED AT (FIXED: Use ownerId)
    // ================================================================

    @Query("SELECT f FROM Favorite f WHERE f.ownerId = :ownerId AND f.isActive = true ORDER BY f.favoritedAt DESC")
    List<Favorite> findFavoritesByOwnerIdOrderByFavoritedAtDesc(@Param("ownerId") Long ownerId);
}