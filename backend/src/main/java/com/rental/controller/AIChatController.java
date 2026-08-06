package com.rental.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rental.dto.VoiceSearchRequest;
import com.rental.dto.VoiceSearchResponse;
import com.rental.service.AIChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping("/voice-search")
    public ResponseEntity<VoiceSearchResponse> voiceSearch(@RequestBody VoiceSearchRequest request) {
        log.info("🎤 Voice search received: {}", request.getQuery());
        
        VoiceSearchResponse response = aiChatService.processVoiceSearch(request.getQuery());
        log.info("📊 Voice search response: aiAvailable={}, totalResults={}", 
            response.isAiAvailable(), response.getTotalResults());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean available = aiChatService.isAvailable();
        log.info("🏥 Health check: aiAvailable={}", available);
        
        return ResponseEntity.ok(Map.of(
            "aiAvailable", available,
            "status", available ? "OK" : "UNAVAILABLE",
            "message", available ? "AI service is running" : "AI service is not available"
        ));
    }
}