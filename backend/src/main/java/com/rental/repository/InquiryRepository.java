package com.rental.repository;

import com.rental.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByPropertyId(Long propertyId);

    List<Inquiry> findBySenderId(Long senderId);

    List<Inquiry> findByPropertyIdAndSenderId(Long propertyId, Long senderId);

    // Get inquiries for properties owned by an owner
    @Query("SELECT i FROM Inquiry i WHERE i.propertyId IN (SELECT p.id FROM Property p WHERE p.ownerId = :ownerId)")
    List<Inquiry> findInquiriesForOwner(@Param("ownerId") Long ownerId);

    // Count unread inquiries for an owner
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.propertyId IN (SELECT p.id FROM Property p WHERE p.ownerId = :ownerId) AND i.status = 'NEW'")
    long countUnreadForOwner(@Param("ownerId") Long ownerId);

    // Mark inquiry as read
    @Modifying
    @Transactional
    @Query("UPDATE Inquiry i SET i.status = 'READ' WHERE i.id = :inquiryId")
    void markAsRead(@Param("inquiryId") Long inquiryId);
}