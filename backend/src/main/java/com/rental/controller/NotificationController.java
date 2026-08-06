package com.rental.controller;

import com.rental.entity.Notification;
import com.rental.security.OwnerPrincipal;
import com.rental.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal OwnerPrincipal principal) {
        // ✅ Fix: Return empty list if principal is null (unauthenticated)
        if (principal == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Notification> notifications = notificationService.getNotificationsForOwner(principal.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal OwnerPrincipal principal) {
        // ✅ Fix: Return 0 if principal is null (unauthenticated)
        if (principal == null) {
            return ResponseEntity.ok(0L);
        }
        long count = notificationService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal OwnerPrincipal principal) {
        // ✅ Fix: Return OK if principal is null (unauthenticated)
        if (principal == null) {
            return ResponseEntity.ok().build();
        }
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId,
                                           @AuthenticationPrincipal OwnerPrincipal principal) {
        // ✅ Fix: Return OK if principal is null (unauthenticated)
        if (principal == null) {
            return ResponseEntity.ok().build();
        }
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}