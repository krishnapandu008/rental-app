package com.rental.entity;

import com.rental.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Inquiry extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    @Column(name = "property_id", nullable = false)
    private Long propertyId = 0L;

    @Builder.Default
    @Column(name = "sender_id", nullable = false)
    private Long senderId = 0L;

    @Builder.Default
    @Column(nullable = false, length = 500)
    private String message = "";

    @Builder.Default
    @Column(nullable = false)
    private String status = "NEW";

    @Builder.Default
    @Column(length = 500)
    private String reply = "";

    private LocalDateTime repliedAt;

    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    // ✅ Relationship with Owner (sender)
    @ManyToOne
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private Owner sender;

    // Relationship with Property
    @ManyToOne
    @JoinColumn(name = "property_id", insertable = false, updatable = false)
    private Property property;

    // ================================================================
    // HELPER METHODS
    // ================================================================

    public void markAsRead() {
        if (!"READ".equals(status) && !"REPLIED".equals(status)) {
            this.status = "READ";
        }
    }

    public void reply(String replyMessage) {
        if (replyMessage != null && !replyMessage.trim().isEmpty()) {
            this.reply = replyMessage;
            this.status = "REPLIED";
            this.repliedAt = LocalDateTime.now();
        }
    }

    public boolean isNew() {
        return "NEW".equals(status);
    }

    public boolean isRead() {
        return "READ".equals(status);
    }

    public boolean isReplied() {
        return "REPLIED".equals(status);
    }

    public boolean hasReply() {
        return reply != null && !reply.trim().isEmpty();
    }

    public String getSenderName() {
        return sender != null ? sender.getName() : null;
    }

    public String getSenderEmail() {
        return sender != null ? sender.getEmail() : null;
    }

    public String getPropertyTitle() {
        return property != null ? property.getTitle() : null;
    }

    public String getPropertyLocation() {
        return property != null ? property.getLocationDisplayName() : null;
    }

    public Long getOwnerId() {
        return property != null ? property.getOwnerId() : null;
    }

    public String getOwnerName() {
        if (property != null && property.getOwner() != null) {
            return property.getOwner().getName();
        }
        return null;
    }

    public String getStatusDisplayName() {
        return switch (status) {
            case "NEW" -> "New";
            case "READ" -> "Read";
            case "REPLIED" -> "Replied";
            default -> status;
        };
    }

    @Override
    public String toString() {
        return String.format("Inquiry{id=%d, propertyId=%d, senderId=%d, status='%s'}", 
            getId(), propertyId, senderId, status);
    }
}