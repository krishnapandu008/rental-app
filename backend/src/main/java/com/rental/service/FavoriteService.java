package com.rental.service;

import com.rental.entity.Favorite;
import com.rental.exception.ResourceNotFoundException;
import com.rental.repository.FavoriteRepository;
import com.rental.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public boolean toggleFavorite(Long ownerId, Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found");
        }
        var existing = favoriteRepository.findByOwnerIdAndPropertyId(ownerId, propertyId);
        if (existing.isPresent()) {
            favoriteRepository.deleteByOwnerIdAndPropertyId(ownerId, propertyId);
            return false;
        } else {
            Favorite favorite = Favorite.builder()
                    .ownerId(ownerId)
                    .propertyId(propertyId)
                    .build();
            favoriteRepository.save(favorite);
            return true;
        }
    }

    public List<Long> getFavoritePropertyIds(Long ownerId) {
        return favoriteRepository.findByOwnerId(ownerId)
                .stream()
                .map(Favorite::getPropertyId)
                .collect(Collectors.toList());
    }

    public boolean isFavorited(Long ownerId, Long propertyId) {
        return favoriteRepository.findByOwnerIdAndPropertyId(ownerId, propertyId).isPresent();
    }
}