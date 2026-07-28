package com.rental.repository;

import com.rental.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.ownerId = :ownerId AND n.isRead = false")
    long countUnreadByOwnerId(@Param("ownerId") Long ownerId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.ownerId = :ownerId AND n.isRead = false")
    void markAllAsRead(@Param("ownerId") Long ownerId);
}