package com.rental.entity.base;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    @Builder.Default  // ✅ Add this for line 38
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    @Builder.Default  // ✅ Add this for line 73
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ================================================================
    // HELPER METHODS
    // ================================================================

    public boolean isActive() {
        return isActive != null && isActive && !Boolean.TRUE.equals(isDeleted);
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (!active) {
            this.deletedAt = LocalDateTime.now();
        } else {
            this.deletedAt = null;
        }
    }

    public void softDelete() {
        this.isActive = false;
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isActive = true;
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(isDeleted);
    }

    @Override
    public String toString() {
        return String.format("BaseEntity{id=%d, isActive=%s, isDeleted=%s}", 
            id, isActive, isDeleted);
    }
}