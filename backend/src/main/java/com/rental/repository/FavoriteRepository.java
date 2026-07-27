package com.rental.repository;

import com.rental.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByOwnerIdAndPropertyId(Long ownerId, Long propertyId);

    List<Favorite> findByOwnerId(Long ownerId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM favorites WHERE owner_id = :ownerId AND property_id = :propertyId", nativeQuery = true)
    int deleteByOwnerIdAndPropertyIdNative(@Param("ownerId") Long ownerId, @Param("propertyId") Long propertyId);
}