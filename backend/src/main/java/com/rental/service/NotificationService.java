package com.rental.service;

import com.rental.dto.NotificationResponseDto;
import com.rental.entity.Notification;
import com.rental.exception.ResourceNotFoundException;
import com.rental.mapper.NotificationMapper;
import com.rental.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    // ================================================================
    // CREATE NOTIFICATIONS
    // ================================================================

    public Notification createNotification(Long ownerId, String type, String title,
                                           String message, String link, Long relatedId) {
        log.info("Creating notification for owner: {}", ownerId);

        Notification notification = Notification.builder()
                .ownerId(ownerId)  // ✅ Changed from userId
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .relatedId(relatedId)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with ID: {}", saved.getId());
        return saved;
    }

    // Convenience method for inquiry notifications
    public void createInquiryNotification(Long ownerId, String senderName, String propertyTitle, Long inquiryId) {
        String title = "New Inquiry";
        String message = senderName + " sent a message about \"" + propertyTitle + "\"";
        String link = "/inquiry/" + inquiryId;
        createNotification(ownerId, "INQUIRY", title, message, link, inquiryId);
    }

    // Convenience method for reply notifications
    public void createReplyNotification(Long senderId, String ownerName, String propertyTitle, Long inquiryId) {
        String title = "Reply to Your Inquiry";
        String message = ownerName + " replied to your inquiry about \"" + propertyTitle + "\"";
        String link = "/inquiry/" + inquiryId;
        createNotification(senderId, "REPLY", title, message, link, inquiryId);
    }

    // ================================================================
    // GET NOTIFICATIONS
    // ================================================================

    public List<NotificationResponseDto> getNotificationsForUser(Long ownerId) {
        log.info("Getting notifications for owner: {}", ownerId);
        return notificationRepository.findByOwnerIdOrderBySentAtDesc(ownerId)
                .stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCountForUser(Long ownerId) {
        return notificationRepository.countByOwnerIdAndIsReadFalse(ownerId);
    }

    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    // ================================================================
    // UPDATE NOTIFICATIONS
    // ================================================================

    public void markAsRead(Long notificationId) {
        log.info("Marking notification as read: {}", notificationId);
        Notification notification = getNotificationById(notificationId);
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long ownerId) {
        log.info("Marking all notifications as read for owner: {}", ownerId);
        List<Notification> notifications = notificationRepository.findByOwnerIdAndIsReadFalse(ownerId);
        notifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(notifications);
    }

    // ================================================================
    // DELETE NOTIFICATIONS
    // ================================================================

    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification: {}", notificationId);
        notificationRepository.deleteById(notificationId);
    }
}