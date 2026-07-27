package com.rental.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rental.security.OwnerPrincipal;
import com.rental.service.FavoriteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{propertyId}")
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable Long propertyId,
                                                  @AuthenticationPrincipal OwnerPrincipal principal) {
        boolean isFavorited = favoriteService.toggleFavorite(principal.getId(), propertyId);
        return ResponseEntity.ok(isFavorited);
    }

    @GetMapping
    public List<Long> getFavorites(@AuthenticationPrincipal OwnerPrincipal principal) {
        return favoriteService.getFavoritePropertyIds(principal.getId());
    }

    @GetMapping("/{propertyId}/status")
    public ResponseEntity<Boolean> isFavorited(@PathVariable Long propertyId,
                                               @AuthenticationPrincipal OwnerPrincipal principal) {
        boolean isFavorited = favoriteService.isFavorited(principal.getId(), propertyId);
        return ResponseEntity.ok(isFavorited);
    }
}