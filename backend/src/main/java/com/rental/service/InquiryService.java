package com.rental.service;

import com.rental.dto.InquiryReplyDto;
import com.rental.dto.InquiryResponseDto;
import com.rental.entity.Inquiry;
import com.rental.entity.Owner;
import com.rental.entity.Property;
import com.rental.exception.ResourceNotFoundException;
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
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PropertyRepository propertyRepository;
    private final OwnerService ownerService;

    @Transactional
    public Inquiry createInquiry(Long propertyId, Long senderId, String message) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        Inquiry inquiry = Inquiry.builder()
                .propertyId(propertyId)
                .senderId(senderId)
                .message(message)
                .status("NEW")
                .build();

        return inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getInquiriesForProperty(Long propertyId) {
        return inquiryRepository.findByPropertyId(propertyId);
    }

    public List<Inquiry> getInquiriesForOwner(Long ownerId) {
        return inquiryRepository.findInquiriesForOwner(ownerId);
    }

    public long getUnreadCountForOwner(Long ownerId) {
        return inquiryRepository.countUnreadForOwner(ownerId);
    }

    @Transactional
    public void markAsRead(Long inquiryId) {
        inquiryRepository.markAsRead(inquiryId);
    }

    @Transactional
    public Inquiry replyToInquiry(Long inquiryId, InquiryReplyDto dto, Long ownerId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));

        // Verify the property belongs to this owner
        Property property = propertyRepository.findById(inquiry.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (!property.getOwnerId().equals(ownerId)) {
            throw new SecurityException("You are not authorized to reply to this inquiry");
        }

        inquiry.setReply(dto.getReply());
        inquiry.setStatus("REPLIED");
        inquiry.setRepliedAt(LocalDateTime.now());

        return inquiryRepository.save(inquiry);
    }

    // Convert to DTO with property details
    public List<InquiryResponseDto> getInquiriesForOwnerWithDetails(Long ownerId) {
        List<Inquiry> inquiries = inquiryRepository.findInquiriesForOwner(ownerId);
        return inquiries.stream()
                .map(i -> {
                    Property property = propertyRepository.findById(i.getPropertyId()).orElse(null);
                    Owner sender = ownerService.findById(i.getSenderId());
                    return InquiryResponseDto.builder()
                            .id(i.getId())
                            .propertyId(i.getPropertyId())
                            .propertyTitle(property != null ? property.getTitle() : "Unknown")
                            .senderName(sender != null ? sender.getName() : "Unknown")
                            .senderEmail(sender != null ? sender.getEmail() : "Unknown")
                            .message(i.getMessage())
                            .reply(i.getReply())
                            .status(i.getStatus())
                            .createdAt(i.getCreatedAt())
                            .repliedAt(i.getRepliedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}