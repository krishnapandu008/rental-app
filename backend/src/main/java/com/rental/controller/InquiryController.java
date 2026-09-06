package com.rental.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rental.dto.InquiryReplyDto;
import com.rental.dto.InquiryRequestDto;
import com.rental.dto.InquiryResponseDto;
import com.rental.entity.Inquiry;
import com.rental.entity.Property;
import com.rental.enums.UserRole;
import com.rental.exception.ForbiddenException;
import com.rental.mapper.InquiryMapper;
import com.rental.security.OwnerPrincipal;
import com.rental.service.InquiryService;
import com.rental.service.PropertyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final PropertyService propertyService;
    private final InquiryMapper inquiryMapper;
    
    @PostMapping("/{propertyId}")
    public ResponseEntity<Inquiry> createInquiry(
            @PathVariable Long propertyId,
            @Valid @RequestBody InquiryRequestDto dto,
            @AuthenticationPrincipal OwnerPrincipal principal) {
        Inquiry inquiry = inquiryService.createInquiry(
                propertyId,
                principal.getId(),
                dto.getMessage()
        );
        return ResponseEntity.ok(inquiry);
    }

    @GetMapping("/property/{propertyId}")
    public List<InquiryResponseDto> getInquiriesForProperty(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal OwnerPrincipal principal) {
        
        // 1. Fetch property to check ownership
        Property property = propertyService.getPropertyEntityById(propertyId);
        
        // 2. Authorization check: only property owner or ADMIN can view
        if (!principal.getId().equals(property.getOwnerId()) && 
            !principal.getRole().equals(UserRole.ADMIN)) {
            throw new ForbiddenException("You are not authorized to view inquiries for this property");
        }
        
        // 3. Return DTOs instead of raw entities
        return inquiryService.getInquiryDtosForProperty(propertyId);
    }

    @GetMapping("/my-inquiries")
    public List<InquiryResponseDto> getMyInquiries(@AuthenticationPrincipal OwnerPrincipal principal) {
        return inquiryService.getInquiriesForOwnerWithDetails(principal.getId());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal OwnerPrincipal principal) {
        long count = inquiryService.getUnreadCountForOwner(principal.getId());
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{inquiryId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long inquiryId) {
        inquiryService.markAsRead(inquiryId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{inquiryId}/reply")
    public ResponseEntity<Inquiry> replyToInquiry(
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryReplyDto dto,
            @AuthenticationPrincipal OwnerPrincipal principal) {
        Inquiry inquiry = inquiryService.replyToInquiry(inquiryId, dto, principal.getId());
        return ResponseEntity.ok(inquiry);
    }

    // ✅ FIXED: Get a single inquiry by ID
    @GetMapping("/{inquiryId}")
    public ResponseEntity<InquiryResponseDto> getInquiryById(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal OwnerPrincipal principal) {
        
        // Fetch the inquiry
        Inquiry inquiry = inquiryService.getInquiryById(inquiryId);
        
        // ✅ FIXED: Use the repository directly to get the Property entity
        // This avoids the 3-parameter method signature issue
        Property property = propertyService.getPropertyEntityById(inquiry.getPropertyId());
        
        // Verify the user is either the sender or the property owner
        if (!principal.getId().equals(inquiry.getSenderId()) && 
            !principal.getId().equals(property.getOwnerId())) {
            throw new ForbiddenException("You are not authorised to view this inquiry");
        }
        
        // Convert to DTO with details
        InquiryResponseDto response = inquiryService.getInquiryResponseDto(inquiry);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/sent")
    public List<InquiryResponseDto> getSentInquiries(@AuthenticationPrincipal OwnerPrincipal principal) {
        return inquiryService.getSentInquiries(principal.getId());
    }
    @GetMapping("/my-all")
    public List<InquiryResponseDto> getAllMyInquiries(@AuthenticationPrincipal OwnerPrincipal principal) {
        return inquiryService.getAllMyInquiries(principal.getId());
    }
}