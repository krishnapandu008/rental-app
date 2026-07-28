package com.rental.service;

import com.rental.entity.Notification;
import com.rental.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(Long ownerId, String type, String title,
                                           String message, String link, Long relatedId) {
        Notification notification = Notification.builder()
                .ownerId(ownerId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .relatedId(relatedId)
                .isRead(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        log.info("📨 Notification created for owner {}: {}", ownerId, title);
        return saved;
    }

    public List<Notification> getNotificationsForOwner(Long ownerId) {
        return notificationRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public long getUnreadCount(Long ownerId) {
        return notificationRepository.countUnreadByOwnerId(ownerId);
    }

    @Transactional
    public void markAllAsRead(Long ownerId) {
        notificationRepository.markAllAsRead(ownerId);
        log.info("📨 All notifications marked as read for owner {}", ownerId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                    log.info("📨 Notification {} marked as read", notificationId);
                });
    }
}