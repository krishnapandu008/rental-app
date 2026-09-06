package com.rental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.rental.entity.Inquiry;
import com.rental.repository.base.BaseRepository;

public interface InquiryRepository extends BaseRepository<Inquiry, Long> {

    List<Inquiry> findByPropertyId(Long propertyId);

    List<Inquiry> findBySenderId(Long senderId);

    List<Inquiry> findByPropertyIdAndSenderId(Long propertyId, Long senderId);

    // ✅ Fixed: Use owner.id instead of ownerId
    @Query("SELECT i FROM Inquiry i WHERE i.propertyId IN (SELECT p.id FROM Property p WHERE p.owner.id = :ownerId)")
    List<Inquiry> findInquiriesForOwner(@Param("ownerId") Long ownerId);

    // ✅ Fixed: Use owner.id instead of ownerId
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.propertyId IN (SELECT p.id FROM Property p WHERE p.owner.id = :ownerId) AND i.status = 'NEW'")
    long countUnreadForOwner(@Param("ownerId") Long ownerId);

    // Mark inquiry as read
    @Modifying
    @Transactional
    @Query("UPDATE Inquiry i SET i.status = 'READ' WHERE i.id = :inquiryId")
    void markAsRead(@Param("inquiryId") Long inquiryId);

    // ✅ Fixed: Use owner.id instead of ownerId
    @Query("SELECT i FROM Inquiry i WHERE i.senderId = :userId OR i.propertyId IN (SELECT p.id FROM Property p WHERE p.owner.id = :userId)")
    List<Inquiry> findBySenderIdOrPropertyOwnerId(@Param("userId") Long userId);
}