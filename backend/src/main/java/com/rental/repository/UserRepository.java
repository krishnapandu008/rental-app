package com.rental.repository;

import com.rental.entity.User;
import com.rental.enums.UserRole;
import com.rental.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    // ================================================================
    // AUTHENTICATION & REGISTRATION
    // ================================================================

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ================================================================
    // ROLE-BASED QUERIES
    // ================================================================

    List<User> findByRole(UserRole role);

    Page<User> findByRole(UserRole role, Pageable pageable);

    List<User> findByRoleAndIsActiveTrue(UserRole role);

    // ================================================================
    // SEARCH & FILTER
    // ================================================================

    List<User> findByNameContainingIgnoreCase(String name);

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<User> findByEmailContainingIgnoreCase(String email);

    // ================================================================
    // VERIFICATION
    // ================================================================

    List<User> findByIsVerifiedTrue();

    List<User> findByIsVerifiedFalse();

    Page<User> findByIsVerifiedTrue(Pageable pageable);

    long countByIsVerifiedTrue();

    long countByIsVerifiedFalse();

    // ================================================================
    // LAST LOGIN TRACKING
    // ================================================================

    List<User> findByLastLoginAtBefore(LocalDateTime dateTime);

    List<User> findByLastLoginAtAfter(LocalDateTime dateTime);

    // ================================================================
    // ADVANCED QUERIES
    // ================================================================

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true AND u.isVerified = true")
    List<User> findActiveVerifiedUsersByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:isVerified IS NULL OR u.isVerified = :isVerified)")
    Page<User> searchUsers(@Param("search") String search,
                           @Param("role") UserRole role,
                           @Param("isVerified") Boolean isVerified,
                           Pageable pageable);

    // ================================================================
    // DASHBOARD STATISTICS
    // ================================================================

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveByRole(@Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersRegisteredAfter(@Param("startDate") LocalDateTime startDate);
}