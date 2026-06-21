package com.rental.controller;

import com.rental.dto.PropertyRequestDto;
import com.rental.dto.PropertyResponseDto;
import com.rental.service.PropertyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

    @GetMapping
    public List<PropertyResponseDto> getAllAvailable() {
        return propertyService.getAllAvailable();
    }

    @GetMapping("/owner/{ownerId}")
    public List<PropertyResponseDto> getByOwner(@PathVariable Long ownerId) {
        return propertyService.getByOwner(ownerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponseDto create(@Valid @RequestBody PropertyRequestDto dto, HttpServletRequest request) {
        Long ownerId = (Long) request.getAttribute("ownerId");
        if (ownerId == null) {
            throw new RuntimeException("Unauthorized: JWT token required");
        }
        if (!ownerId.equals(dto.getOwnerId())) {
            throw new RuntimeException("Forbidden: Cannot create property for another owner");
        }
        return propertyService.create(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        Long ownerId = (Long) request.getAttribute("ownerId");
        if (ownerId == null) {
            throw new RuntimeException("Unauthorized: JWT token required");
        }
        propertyService.deleteByIdAndOwnerId(id, ownerId);
    }
}