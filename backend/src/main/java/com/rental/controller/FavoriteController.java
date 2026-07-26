package com.rental.controller;

import com.rental.security.OwnerPrincipal;
import com.rental.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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