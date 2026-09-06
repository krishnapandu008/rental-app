package com.rental.service;

import com.rental.dto.InquiryReplyDto;
import com.rental.dto.InquiryResponseDto;
import com.rental.entity.Inquiry;
import com.rental.entity.Owner;
import com.rental.entity.Property;
import com.rental.exception.ResourceNotFoundException;
import com.rental.mapper.InquiryMapper;
import com.rental.repository.InquiryRepository;
import com.rental.repository.PropertyRepository;
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
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PropertyRepository propertyRepository;
    private final OwnerService ownerService;
    private final NotificationService notificationService;
    private final InquiryMapper inquiryMapper;

    // ================================================================
    // CREATE INQUIRY
    // ================================================================

    @Transactional
    public Inquiry createInquiry(Long propertyId, Long senderId, String message) {
        log.info("Creating inquiry for property: {} from sender: {}", propertyId, senderId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        Owner sender = ownerService.findById(senderId);

        Long propertyOwnerId = property.getOwner() != null ? property.getOwner().getId() : null;
        if (propertyOwnerId == null) {
            throw new ResourceNotFoundException("Property owner not found");
        }

        Inquiry inquiry = Inquiry.builder()
                .propertyId(propertyId)
                .senderId(senderId)
                .message(message)
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);
        log.info("Inquiry created with ID: {}", saved.getId());

        // ✅ Delegate notification creation to NotificationService
        notificationService.createInquiryNotification(
                propertyOwnerId,
                sender.getName(),
                property.getTitle(),
                saved.getId()
        );

        return saved;
    }

    // ================================================================
    // GET INQUIRIES
    // ================================================================

    public List<Inquiry> getInquiriesForProperty(Long propertyId) {
        log.info("Getting inquiries for property: {}", propertyId);
        return inquiryRepository.findByPropertyId(propertyId);
    }

    public List<Inquiry> getInquiriesForOwner(Long ownerId) {
        log.info("Getting inquiries for owner: {}", ownerId);
        return inquiryRepository.findInquiriesForOwner(ownerId);
    }

    public long getUnreadCountForOwner(Long ownerId) {
        log.info("Getting unread count for owner: {}", ownerId);
        return inquiryRepository.countUnreadForOwner(ownerId);
    }

    public Inquiry getInquiryById(Long inquiryId) {
        log.info("Getting inquiry by ID: {}", inquiryId);
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));
    }

    // ================================================================
    // UPDATE INQUIRIES
    // ================================================================

    @Transactional
    public void markAsRead(Long inquiryId) {
        log.info("Marking inquiry as read: {}", inquiryId);
        inquiryRepository.markAsRead(inquiryId);
        log.info("Inquiry marked as read: {}", inquiryId);
    }

    @Transactional
    public Inquiry replyToInquiry(Long inquiryId, InquiryReplyDto dto, Long ownerId) {
        log.info("Replying to inquiry: {} by owner: {}", inquiryId, ownerId);

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));

        Property property = propertyRepository.findById(inquiry.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        Long propertyOwnerId = property.getOwner() != null ? property.getOwner().getId() : null;
        if (propertyOwnerId == null || !propertyOwnerId.equals(ownerId)) {
            throw new SecurityException("You are not authorized to reply to this inquiry");
        }

        Owner propertyOwner = ownerService.findById(ownerId);
        Owner sender = ownerService.findById(inquiry.getSenderId());

        inquiry.reply(dto.getReply());

        Inquiry saved = inquiryRepository.save(inquiry);
        log.info("Reply sent to inquiry: {}", inquiryId);

        // ✅ Delegate notification creation to NotificationService
        notificationService.createReplyNotification(
                sender.getId(),
                propertyOwner.getName(),
                property.getTitle(),
                saved.getId()
        );

        return saved;
    }

    // ================================================================
    // GET INQUIRIES WITH DETAILS (Using Mapper)
    // ================================================================

    public List<InquiryResponseDto> getInquiriesForOwnerWithDetails(Long ownerId) {
        log.info("Getting inquiries with details for owner: {}", ownerId);
        List<Inquiry> inquiries = inquiryRepository.findInquiriesForOwner(ownerId);
        return inquiries.stream()
                .map(inquiryMapper::toDto)
                .collect(Collectors.toList());
    }

    public InquiryResponseDto getInquiryResponseDto(Inquiry inquiry) {
        log.info("Converting inquiry to DTO: {}", inquiry.getId());
        return inquiryMapper.toDto(inquiry);
    }

    public List<InquiryResponseDto> getSentInquiries(Long senderId) {
        log.info("Getting sent inquiries for sender: {}", senderId);
        List<Inquiry> inquiries = inquiryRepository.findBySenderId(senderId);
        return inquiries.stream()
                .map(inquiry -> {
                    InquiryResponseDto dto = inquiryMapper.toDto(inquiry);
                    dto.setSenderName("You");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<InquiryResponseDto> getAllMyInquiries(Long userId) {
        log.info("Getting all inquiries for user: {}", userId);
        List<Inquiry> inquiries = inquiryRepository.findBySenderIdOrPropertyOwnerId(userId);
        return inquiries.stream()
                .map(inquiry -> {
                    InquiryResponseDto dto = inquiryMapper.toDto(inquiry);
                    
                    Property property = propertyRepository.findById(inquiry.getPropertyId()).orElse(null);
                    boolean isReceived = property != null &&
                                        property.getOwner() != null &&
                                        property.getOwner().getId().equals(userId);
                    boolean isSent = inquiry.getSenderId().equals(userId);

                    if (isSent) {
                        dto.setSenderName("You");
                    }
                    dto.setIsReceived(isReceived);
                    dto.setIsSent(isSent);
                    return dto;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    public List<InquiryResponseDto> getInquiryDtosForProperty(Long propertyId) {
        log.info("Getting inquiry DTOs for property: {}", propertyId);
        return inquiryRepository.findByPropertyId(propertyId).stream()
                .map(inquiryMapper::toDto)
                .collect(Collectors.toList());
    }
}