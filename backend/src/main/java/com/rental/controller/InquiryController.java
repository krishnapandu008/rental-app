package com.rental.controller;

import com.rental.dto.InquiryReplyDto;
import com.rental.dto.InquiryRequestDto;
import com.rental.dto.InquiryResponseDto;
import com.rental.entity.Inquiry;
import com.rental.security.OwnerPrincipal;
import com.rental.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

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
    public List<Inquiry> getInquiriesForProperty(@PathVariable Long propertyId) {
        return inquiryService.getInquiriesForProperty(propertyId);
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
}