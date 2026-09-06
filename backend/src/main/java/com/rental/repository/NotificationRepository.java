package com.rental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.rental.entity.Notification;
import com.rental.repository.base.BaseRepository;

public interface NotificationRepository extends BaseRepository<Notification, Long> {

    // ✅ Fixed: Use ownerId instead of userId
    @Query("SELECT n FROM Notification n WHERE n.ownerId = :ownerId ORDER BY n.sentAt DESC")
    List<Notification> findByOwnerIdOrderBySentAtDesc(@Param("ownerId") Long ownerId);

    @Query("SELECT n FROM Notification n WHERE n.ownerId = :ownerId AND n.isRead = false")
    List<Notification> findByOwnerIdAndIsReadFalse(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.ownerId = :ownerId AND n.isRead = false")
    long countByOwnerIdAndIsReadFalse(@Param("ownerId") Long ownerId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.ownerId = :ownerId AND n.isRead = false")
    void markAllAsRead(@Param("ownerId") Long ownerId);
}