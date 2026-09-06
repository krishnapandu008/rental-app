package com.rental.location.service;

import com.rental.location.domain.Area;
import com.rental.location.domain.LocationData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final List<LocationData> locationDataList;
    private LocationData currentLocation;

    @PostConstruct
    public void init() {
        // Set Kuppam as the default/current location
        this.currentLocation = locationDataList.stream()
                .filter(loc -> "kuppam".equals(loc.getLocationId()))
                .findFirst()
                .orElse(null);

        if (currentLocation != null) {
            log.info("📍 LocationService initialized with: {}", currentLocation.getDisplayName());
            log.info("📊 Loaded {} areas", currentLocation.getAreas().size());
            log.info("🛣️  Loaded {} streets", currentLocation.getAllStreets().size());
        } else {
            log.warn("⚠️ No default location found!");
        }
    }

    public LocationData getCurrentLocation() {
        return currentLocation;
    }

    public List<String> getLocationSuggestions(String query) {
        if (currentLocation == null) return List.of();

        List<String> results = new ArrayList<>();

        // For empty query, return area names
        if (query == null || query.isEmpty()) {
            return currentLocation.getAreas().stream()
                    .map(Area::getName)
                    .collect(Collectors.toList());
        }

        String lower = query.toLowerCase().trim();

        // Check if query matches Kuppam or related terms
        if (currentLocation.supportsLocation(query)) {
            // Add area names
            for (Area area : currentLocation.getAreas()) {
                results.add(area.getName());
                // If query matches area, add streets too
                if (area.getName().toLowerCase().contains(lower) ||
                    lower.contains(area.getName().toLowerCase().substring(0, Math.min(3, area.getName().length())))) {
                    results.addAll(area.getStreets());
                }
            }
        }

        // If no results, return only areas
        if (results.isEmpty()) {
            return currentLocation.getAreas().stream()
                    .map(Area::getName)
                    .collect(Collectors.toList());
        }

        return results.stream().distinct().limit(20).collect(Collectors.toList());
    }

    public boolean isLocationSupported(String query) {
        if (currentLocation == null || query == null) return false;
        return currentLocation.supportsLocation(query);
    }

    public Map<String, Object> getLocationInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("location", currentLocation.getDisplayName());
        info.put("district", currentLocation.getDistrict());
        info.put("state", currentLocation.getState());
        info.put("pinCode", currentLocation.getPinCode());
        info.put("latitude", currentLocation.getDefaultLatitude());
        info.put("longitude", currentLocation.getDefaultLongitude());

        List<Map<String, Object>> areaList = new ArrayList<>();
        for (Area area : currentLocation.getAreas()) {
            Map<String, Object> areaMap = new LinkedHashMap<>();
            areaMap.put("id", area.getId());
            areaMap.put("name", area.getName());
            areaMap.put("latitude", area.getLatitude());
            areaMap.put("longitude", area.getLongitude());
            areaMap.put("streets", area.getStreets());
            areaList.add(areaMap);
        }
        info.put("areas", areaList);

        return info;
    }

    public String getLocationDisplay() {
        if (currentLocation == null) return "Location not set";
        return String.format("📍 %s, %s District, %s - %s",
                currentLocation.getDisplayName(),
                currentLocation.getDistrict(),
                currentLocation.getState(),
                currentLocation.getPinCode()
        );
    }

    // For future expansion - easily add new locations
    public void switchLocation(String locationId) {
        locationDataList.stream()
                .filter(loc -> loc.getLocationId().equals(locationId))
                .findFirst()
                .ifPresent(loc -> {
                    this.currentLocation = loc;
                    log.info("📍 Switched to: {}", loc.getDisplayName());
                });
    }
}