package com.rental.repository;

import com.rental.entity.Address;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends BaseRepository<Address, Long> {

    // ================================================================
    // FIND BY LOCATION
    // ================================================================

    List<Address> findByLocationId(Long locationId);

    Page<Address> findByLocationId(Long locationId, Pageable pageable);

    List<Address> findByLocationIdAndIsActiveTrue(Long locationId);

    // ================================================================
    // FIND BY AREA
    // ================================================================

    List<Address> findByAreaName(String areaName);

    List<Address> findByAreaNameContainingIgnoreCase(String areaName);

    Page<Address> findByAreaNameContainingIgnoreCase(String areaName, Pageable pageable);

    // ================================================================
    // FIND BY STREET
    // ================================================================

    List<Address> findByStreetName(String streetName);

    List<Address> findByStreetNameContainingIgnoreCase(String streetName);

    // ================================================================
    // FIND BY PROPERTY
    // ================================================================

    Optional<Address> findByPropertyId(Long propertyId);

    // ================================================================
    // SEARCH ADDRESSES
    // ================================================================

    @Query("SELECT a FROM Address a WHERE " +
           "(:search IS NULL OR LOWER(a.streetName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.areaName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.landmark) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:locationId IS NULL OR a.location.id = :locationId) AND " +
           "(:isActive IS NULL OR a.isActive = :isActive)")
    Page<Address> searchAddresses(@Param("search") String search,
                                  @Param("locationId") Long locationId,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);

    // ================================================================
    // FIND BY LOCATION WITH AREA
    // ================================================================

    @Query("SELECT a FROM Address a WHERE a.location.id = :locationId AND LOWER(a.areaName) LIKE LOWER(CONCAT('%', :area, '%'))")
    List<Address> findByLocationIdAndAreaNameContaining(@Param("locationId") Long locationId,
                                                        @Param("area") String area);

    // ================================================================
    // FIND DISTINCT AREAS
    // ================================================================

    @Query("SELECT DISTINCT a.areaName FROM Address a WHERE a.location.id = :locationId AND a.isActive = true")
    List<String> findDistinctAreasByLocationId(@Param("locationId") Long locationId);

    @Query("SELECT DISTINCT a.areaName FROM Address a WHERE a.isActive = true")
    List<String> findAllDistinctAreas();

    // ================================================================
    // FIND DISTINCT STREETS
    // ================================================================

    @Query("SELECT DISTINCT a.streetName FROM Address a WHERE a.location.id = :locationId AND a.isActive = true")
    List<String> findDistinctStreetsByLocationId(@Param("locationId") Long locationId);

    // ================================================================
    // STATISTICS
    // ================================================================

    @Query("SELECT COUNT(a) FROM Address a WHERE a.location.id = :locationId")
    long countByLocationId(@Param("locationId") Long locationId);

    @Query("SELECT COUNT(a) FROM Address a WHERE a.isActive = true")
    long countActiveAddresses();

    // ================================================================
    // BULK OPERATIONS
    // ================================================================

    @Query("UPDATE Address a SET a.isActive = false WHERE a.location.id = :locationId")
    void deactivateByLocationId(@Param("locationId") Long locationId);
}