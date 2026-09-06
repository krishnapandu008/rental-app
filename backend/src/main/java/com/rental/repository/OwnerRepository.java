package com.rental.repository;

import com.rental.entity.Owner;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends BaseRepository<Owner, Long> {
    
    Optional<Owner> findByEmail(String email);

    Optional<Owner> findByEmailAndIsActiveTrue(String email);

    Optional<Owner> findByIdAndIsActiveTrue(Long id);

    boolean existsByEmail(String email);

    // ✅ Active users
    Page<Owner> findAllByIsActiveTrue(Pageable pageable);
    
    List<Owner> findAllByIsActiveTrue();

    // ✅ Role-based queries (String for role)
    Page<Owner> findByRole(String role, Pageable pageable);

    List<Owner> findByRole(String role);

    List<Owner> findByRoleAndIsActiveTrue(String role);

    // ✅ Count methods
    @Query("SELECT COUNT(o) FROM Owner o WHERE o.role = :role")
    long countByRole(@Param("role") String role);

    @Query("SELECT COUNT(o) FROM Owner o WHERE o.isActive = true")
    long countByIsActiveTrue();

    @Query("SELECT COUNT(o) FROM Owner o WHERE o.isActive = true AND o.role = :role")
    long countActiveByRole(@Param("role") String role);

    Optional<Owner> findByEmailAndPassword(String email, String password);
}