package com.rental.entity;

import java.time.LocalDateTime;

import com.rental.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notifications")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    @Column(name = "owner_id", nullable = false)
    private Long ownerId = 0L;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String type = "SYSTEM";

    @Builder.Default
    @Column(nullable = false, length = 255)
    private String title = "";

    @Builder.Default
    @Column(nullable = false, length = 500)
    private String message = "";

    @Builder.Default
    @Column(length = 255)
    private String link = "";

    @Builder.Default
    @Column(name = "related_id")
    private Long relatedId = 0L;

    @Builder.Default
    @Column(nullable = false)
    private boolean isRead = false;

    private LocalDateTime readAt;

    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    // Relationship with Owner (not User)
    @ManyToOne
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private Owner owner;

    // ================================================================
    // HELPER METHODS
    // ================================================================

    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    public boolean isUnread() {
        return !isRead;
    }

    public String getOwnerName() {
        return owner != null ? owner.getName() : null;
    }

    public String getOwnerEmail() {
        return owner != null ? owner.getEmail() : null;
    }

    public String getTypeDisplayName() {
        if (type == null) return "System";
        return switch (type.toUpperCase()) {
            case "INQUIRY" -> "New Inquiry";
            case "REPLY" -> "Reply Received";
            case "SYSTEM" -> "System Notification";
            case "VIEW" -> "Property Viewed";
            case "FAVORITE" -> "Property Favorited";
            default -> type;
        };
    }

    @Override
    public String toString() {
        return String.format("Notification{id=%d, ownerId=%d, type='%s', title='%s', isRead=%s}", 
            getId(), ownerId, type, title, isRead);
    }
}