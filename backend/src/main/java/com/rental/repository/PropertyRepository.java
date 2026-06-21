package com.rental.repository;

import com.rental.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByAvailableTrue();
    List<Property> findByOwnerId(Long ownerId);
    List<Property> findByLocationContainingIgnoreCase(String location);
}