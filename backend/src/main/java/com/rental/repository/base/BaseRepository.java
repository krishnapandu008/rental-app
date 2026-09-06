package com.rental.repository.base;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    // ================================================================
    // FIND ACTIVE/INACTIVE
    // ================================================================

    List<T> findAllByIsActiveTrue();

    List<T> findAllByIsActiveFalse();

    Optional<T> findByIdAndIsActiveTrue(ID id);

    Page<T> findAllByIsActiveTrue(Pageable pageable);

    Page<T> findAllByIsActiveFalse(Pageable pageable);

    // ================================================================
    // COUNT METHODS
    // ================================================================

    long countByIsActiveTrue();

    long countByIsActiveFalse();

    // ================================================================
    // SOFT DELETE OPERATIONS
    // ================================================================

    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.isActive = false WHERE e.id = :id")
    void softDeleteById(@Param("id") ID id);

    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.isActive = true WHERE e.id = :id")
    void restoreById(@Param("id") ID id);

    default void softDelete(ID id) {
        softDeleteById(id);
    }

    default void restore(ID id) {
        restoreById(id);
    }

    // ================================================================
    // BULK OPERATIONS
    // ================================================================

    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.isActive = false WHERE e.id IN :ids")
    void softDeleteAllByIds(@Param("ids") List<ID> ids);

    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.isActive = true WHERE e.id IN :ids")
    void restoreAllByIds(@Param("ids") List<ID> ids);

    // ================================================================
    // CHECK EXISTENCE
    // ================================================================

    boolean existsByIdAndIsActiveTrue(ID id);

    boolean existsByIdAndIsActiveFalse(ID id);
}