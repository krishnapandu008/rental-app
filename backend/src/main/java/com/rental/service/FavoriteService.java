package com.rental.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rental.exception.ResourceNotFoundException;
import com.rental.repository.PropertyRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {
    private final PropertyRepository propertyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public boolean toggleFavorite(Long ownerId, Long propertyId) {
        log.info("Toggling favorite for owner: {} and property: {}", ownerId, propertyId);
        
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found");
        }

        String countQuery = "SELECT COUNT(*) FROM favorites WHERE owner_id = :ownerId AND property_id = :propertyId";
        Long count = (Long) entityManager.createNativeQuery(countQuery)
                .setParameter("ownerId", ownerId)
                .setParameter("propertyId", propertyId)
                .getSingleResult();

        if (count > 0) {
            String deleteQuery = "DELETE FROM favorites WHERE owner_id = :ownerId AND property_id = :propertyId";
            entityManager.createNativeQuery(deleteQuery)
                    .setParameter("ownerId", ownerId)
                    .setParameter("propertyId", propertyId)
                    .executeUpdate();
            entityManager.flush();
            log.info("Favorite removed for owner: {} and property: {}", ownerId, propertyId);
            return false;
        } else {
            String insertQuery = "INSERT INTO favorites (owner_id, property_id, favorited_at) VALUES (:ownerId, :propertyId, NOW())";
            entityManager.createNativeQuery(insertQuery)
                    .setParameter("ownerId", ownerId)
                    .setParameter("propertyId", propertyId)
                    .executeUpdate();
            log.info("Favorite added for owner: {} and property: {}", ownerId, propertyId);
            return true;
        }
    }

    public List<Long> getFavoritePropertyIds(Long ownerId) {
        log.info("Getting favorite property IDs for owner: {}", ownerId);
        String query = "SELECT property_id FROM favorites WHERE owner_id = :ownerId";
        @SuppressWarnings("unchecked")
        List<Long> propertyIds = entityManager.createNativeQuery(query)
                .setParameter("ownerId", ownerId)
                .getResultList();
        return propertyIds;
    }

    public boolean isFavorited(Long ownerId, Long propertyId) {
        log.info("Checking if property {} is favorited by owner: {}", propertyId, ownerId);
        String query = "SELECT COUNT(*) FROM favorites WHERE owner_id = :ownerId AND property_id = :propertyId";
        Long count = (Long) entityManager.createNativeQuery(query)
                .setParameter("ownerId", ownerId)
                .setParameter("propertyId", propertyId)
                .getSingleResult();
        return count > 0;
    }
}