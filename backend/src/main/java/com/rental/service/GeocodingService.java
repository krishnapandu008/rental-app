package com.rental.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${geocoding.nominatim.url:https://nominatim.openstreetmap.org/search}")
    private String nominatimUrl;

    @Value("${geocoding.nominatim.user-agent:rental-app/1.0}")
    private String userAgent;

    @Value("${geocoding.enabled:true}")
    private boolean geocodingEnabled;

    // ✅ Cache for frequently searched terms
    private final Map<String, List<String>> suggestionCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTime = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 30 * 60 * 1000; // 30 minutes

    /**
     * Get cached suggestions if available and not expired
     */
    private List<String> getCachedSuggestions(String query) {
        if (suggestionCache.containsKey(query)) {
            Long time = cacheTime.get(query);
            if (time != null && System.currentTimeMillis() - time < CACHE_DURATION) {
                log.debug("📦 Cache hit for: '{}'", query);
                return suggestionCache.get(query);
            } else {
                // Cache expired, remove it
                suggestionCache.remove(query);
                cacheTime.remove(query);
            }
        }
        return null;
    }

    /**
     * Cache suggestions for a query
     */
    private void cacheSuggestions(String query, List<String> suggestions) {
        if (suggestions != null && !suggestions.isEmpty()) {
            suggestionCache.put(query, new ArrayList<>(suggestions));
            cacheTime.put(query, System.currentTimeMillis());
            log.debug("💾 Cached suggestions for '{}' ({} items)", query, suggestions.size());
        }
    }

    /**
     * Clear cache (useful for testing)
     */
    public void clearCache() {
        suggestionCache.clear();
        cacheTime.clear();
        log.info("🗑️ Cache cleared");
    }

    /**
     * Get location suggestions from OpenStreetMap Nominatim with enhanced partial/fuzzy matching
     */
    public List<String> getLocationSuggestions(String query) {
        if (!geocodingEnabled || query == null || query.length() < 1) {
            log.info("⚠️ Geocoding disabled or query too short: '{}'", query);
            return List.of();
        }

        String cacheKey = query.toLowerCase().trim();
        
        // ✅ Check cache first
        List<String> cached = getCachedSuggestions(cacheKey);
        if (cached != null) {
            return cached;
        }

        // ✅ For very short queries (1-2 chars), use prepopulated suggestions
        if (query.length() <= 2) {
            List<String> prepopulated = getPrepopulatedSuggestionsInternal(query);
            if (!prepopulated.isEmpty()) {
                log.info("📋 Using prepopulated suggestions for '{}'", query);
                cacheSuggestions(cacheKey, prepopulated);
                return prepopulated;
            }
        }

        try {
            log.info("📍 Geocoding search for: '{}'", query);

            List<String> allSuggestions = new ArrayList<>();
            
            // Strategy 1: Direct search with country filter
            String url1 = UriComponentsBuilder.fromUriString(nominatimUrl)
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", 15)
                    .queryParam("addressdetails", 0)
                    .queryParam("namedetails", 1)
                    .queryParam("countrycodes", "in")
                    .build()
                    .toUriString();
            
            log.debug("🌐 URL 1: {}", url1);
            List<String> results1 = executeSearch(url1);
            allSuggestions.addAll(results1);

            // Strategy 2: If query is 3 characters, try with "city" modifier
            if (query.length() == 3) {
                String cityQuery = query + " city";
                String url2 = UriComponentsBuilder.fromUriString(nominatimUrl)
                        .queryParam("q", cityQuery)
                        .queryParam("format", "json")
                        .queryParam("limit", 10)
                        .queryParam("addressdetails", 0)
                        .queryParam("namedetails", 1)
                        .queryParam("countrycodes", "in")
                        .build()
                        .toUriString();
                
                log.debug("🌐 URL 2 (city): {}", url2);
                List<String> results2 = executeSearch(url2);
                allSuggestions.addAll(results2);
            }

            // Strategy 3: If query is short (1-3 chars), search with wildcard
            if (query.length() <= 3) {
                String wildcardQuery = query + "*";
                String url3 = UriComponentsBuilder.fromUriString(nominatimUrl)
                        .queryParam("q", wildcardQuery)
                        .queryParam("format", "json")
                        .queryParam("limit", 10)
                        .queryParam("addressdetails", 0)
                        .queryParam("namedetails", 1)
                        .queryParam("countrycodes", "in")
                        .build()
                        .toUriString();
                
                log.debug("🌐 URL 3 (wildcard): {}", url3);
                List<String> results3 = executeSearch(url3);
                allSuggestions.addAll(results3);
            }

            // ✅ If still no results and query length is 3+, try removing last character
            if (allSuggestions.isEmpty() && query.length() >= 3) {
                String shorterQuery = query.substring(0, query.length() - 1);
                String url4 = UriComponentsBuilder.fromUriString(nominatimUrl)
                        .queryParam("q", shorterQuery)
                        .queryParam("format", "json")
                        .queryParam("limit", 10)
                        .queryParam("addressdetails", 0)
                        .queryParam("namedetails", 1)
                        .queryParam("countrycodes", "in")
                        .build()
                        .toUriString();
                
                log.debug("🌐 URL 4 (shorter): {}", url4);
                List<String> results4 = executeSearch(url4);
                allSuggestions.addAll(results4);
            }

            // ✅ Filter suggestions - keep only valid city/town names
            List<String> filteredSuggestions = allSuggestions.stream()
                    .distinct()
                    .filter(s -> s != null && !s.isEmpty())
                    .filter(s -> s.length() >= 2 && s.length() <= 40)
                    .filter(s -> !s.matches(".*[0-9]+.*"))
                    .filter(s -> !s.toLowerCase().contains("road"))
                    .filter(s -> !s.toLowerCase().contains("street"))
                    .filter(s -> !s.toLowerCase().contains("lane"))
                    .filter(s -> !s.toLowerCase().contains("colony"))
                    .filter(s -> !s.toLowerCase().contains("society"))
                    .filter(s -> !s.toLowerCase().contains("building"))
                    .filter(s -> !s.toLowerCase().contains("apartment"))
                    .filter(s -> !s.toLowerCase().contains("complex"))
                    .filter(s -> !s.toLowerCase().contains("chowk"))
                    .filter(s -> !s.toLowerCase().contains("market"))
                    .filter(s -> !s.toLowerCase().contains("station"))
                    .filter(s -> !s.toLowerCase().contains("airport"))
                    .filter(s -> !s.toLowerCase().contains("railway"))
                    // ✅ Prioritize results that start with the query
                    .sorted((a, b) -> {
                        boolean aStarts = a.toLowerCase().startsWith(query.toLowerCase());
                        boolean bStarts = b.toLowerCase().startsWith(query.toLowerCase());
                        if (aStarts && !bStarts) return -1;
                        if (!aStarts && bStarts) return 1;
                        return a.compareTo(b);
                    })
                    .limit(10)
                    .collect(Collectors.toList());

            log.info("✅ Found {} filtered suggestions for '{}'", filteredSuggestions.size(), query);

            if (!filteredSuggestions.isEmpty()) {
                cacheSuggestions(cacheKey, filteredSuggestions);
                return filteredSuggestions;
            }

            // ✅ If no filtered results, return query itself
            log.info("ℹ️ No results found for '{}', returning query itself", query);
            List<String> fallback = List.of(query);
            cacheSuggestions(cacheKey, fallback);
            return fallback;

        } catch (Exception e) {
            log.warn("⚠️ Geocoding failed: {}", e.getMessage());
            return List.of(query);
        }
    }

    /**
     * Execute a search request to Nominatim
     */
    private List<String> executeSearch(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseNominatimResponse(response.getBody());
            }
        } catch (Exception e) {
            log.debug("Search failed for URL: {}", e.getMessage());
        }
        return List.of();
    }

    /**
     * Parse Nominatim JSON response with enhanced filtering
     */
    private List<String> parseNominatimResponse(String responseBody) {
        try {
            List<String> suggestions = new ArrayList<>();
            List<String> seenNames = new ArrayList<>();
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                for (JsonNode result : root) {
                    String displayName = result.path("display_name").asText();
                    String type = result.path("type").asText();
                    String category = result.path("class").asText();

                    if (shouldInclude(category, type)) {
                        String simplified = simplifyLocationName(displayName);
                        if (!simplified.isEmpty() && suggestions.size() < 20) {
                            if (!seenNames.contains(simplified)) {
                                seenNames.add(simplified);
                                suggestions.add(simplified);
                            }
                        }
                    }
                }
            }

            if (suggestions.isEmpty()) {
                for (JsonNode result : root) {
                    String displayName = result.path("display_name").asText();
                    if (displayName != null && !displayName.isEmpty()) {
                        String extracted = extractPlaceName(displayName);
                        if (!extracted.isEmpty() && !seenNames.contains(extracted)) {
                            seenNames.add(extracted);
                            suggestions.add(extracted);
                            if (suggestions.size() >= 10) break;
                        }
                    }
                }
            }

            return suggestions;

        } catch (Exception e) {
            log.error("❌ Error parsing Nominatim response: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Extract just the place name from display name
     */
    private String extractPlaceName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return "";
        }
        
        String[] parts = displayName.split(",");
        if (parts.length > 0) {
            String name = parts[0].trim();
            name = name.replaceAll("\\(.*?\\)", "").trim();
            return name;
        }
        return displayName.trim();
    }

    /**
     * Filter only relevant location types
     */
    private boolean shouldInclude(String category, String type) {
        String[] includeTypes = {
            "city", "town", "village", "neighbourhood", "suburb", 
            "administrative", "locality", "district", "region",
            "state", "country", "hamlet", "borough", "quarter",
            "residential", "commercial"
        };
        
        for (String include : includeTypes) {
            if (category.equalsIgnoreCase(include) || type.equalsIgnoreCase(include)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Simplify location name
     */
    private String simplifyLocationName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return "";
        }

        String[] parts = displayName.split(",");
        if (parts.length > 0) {
            String simplified = parts[0].trim();
            simplified = simplified.replaceAll("\\(.*?\\)", "").trim();
            simplified = simplified.replaceAll("\\s+and\\s+", " ");
            return simplified;
        }
        return displayName;
    }

    /**
     * Public method to get prepopulated suggestions for single letters
     */
    public List<String> getPrepopulatedSuggestions(String query) {
        if (query == null || query.length() != 1) {
            return List.of();
        }
        return getPrepopulatedSuggestionsInternal(query);
    }

    /**
     * Internal method for prepopulated suggestions
     */
    private List<String> getPrepopulatedSuggestionsInternal(String query) {
        String lower = query.toLowerCase().trim();
        
        Map<String, List<String>> prepopulatedMap = new HashMap<>();
        prepopulatedMap.put("a", List.of("Ahmedabad", "Amritsar", "Agra", "Allahabad", "Ajmer", "Aligarh"));
        prepopulatedMap.put("b", List.of("Bangalore", "Bhopal", "Bhubaneswar", "Bandra", "Borivali", "Banaswadi"));
        prepopulatedMap.put("c", List.of("Chennai", "Coimbatore", "Chandigarh", "Calicut", "Cochin", "Cuttack"));
        prepopulatedMap.put("d", List.of("Delhi", "Dehradun", "Dadar", "Durgapur", "Dhanbad", "Dibrugarh"));
        prepopulatedMap.put("e", List.of("Ernakulam", "Erode", "Eluru", "Etawah", "Ettumanoor"));
        prepopulatedMap.put("f", List.of("Faridabad", "Firozabad", "Fatehpur", "Faizabad", "Fazilka"));
        prepopulatedMap.put("g", List.of("Gurgaon", "Ghaziabad", "Gandhinagar", "Guwahati", "Gwalior", "Gorakhpur"));
        prepopulatedMap.put("h", List.of("Hyderabad", "Hubli", "Hosur", "Haldwani", "Haridwar", "Hisar"));
        prepopulatedMap.put("i", List.of("Indore", "Itanagar", "Imphal", "Irinjalakuda", "Ichalkaranji"));
        prepopulatedMap.put("j", List.of("Jaipur", "Jodhpur", "Jabalpur", "Jamshedpur", "Jalandhar", "Jhansi"));
        prepopulatedMap.put("k", List.of("Kolkata", "Kanpur", "Kochi", "Kozhikode", "Kota", "Kharagpur"));
        prepopulatedMap.put("l", List.of("Lucknow", "Ludhiana", "Leh", "Latur", "Loni", "Lakshadweep"));
        prepopulatedMap.put("m", List.of("Mumbai", "Mysore", "Mangalore", "Meerut", "Moradabad", "Madurai"));
        prepopulatedMap.put("n", List.of("Nagpur", "Noida", "Nashik", "Navi Mumbai", "Nellore", "Nagercoil"));
        prepopulatedMap.put("o", List.of("Ooty", "Ongole", "Ozhukarai"));
        prepopulatedMap.put("p", List.of("Pune", "Patna", "Panaji", "Pondicherry", "Porur", "Palakkad"));
        prepopulatedMap.put("r", List.of("Ranchi", "Raipur", "Rajkot", "Rourkela", "Roorkee", "Rewari"));
        prepopulatedMap.put("s", List.of("Surat", "Srinagar", "Shimla", "Siliguri", "Solan", "Satara"));
        prepopulatedMap.put("t", List.of("Thane", "Trivandrum", "Tirupati", "Tirunelveli", "Tuticorin", "Tiruppur"));
        prepopulatedMap.put("u", List.of("Udaipur", "Ujjain", "Unnao", "Udupi", "Uttarkashi"));
        prepopulatedMap.put("v", List.of("Vadodara", "Varanasi", "Vijayawada", "Visakhapatnam", "Vasai", "Vellore"));
        prepopulatedMap.put("w", List.of("Warangal", "Wardha", "Wokha", "Wayanad"));
        prepopulatedMap.put("y", List.of("Yamunanagar", "Yavatmal", "Yercaud"));
        prepopulatedMap.put("z", List.of("Zirakpur", "Zunheboto"));

        // ✅ For single letter
        if (lower.length() == 1) {
            for (String key : prepopulatedMap.keySet()) {
                if (key.equals(lower)) {
                    return prepopulatedMap.get(key);
                }
            }
            return List.of();
        }

        // ✅ For 2+ letters
        for (String key : prepopulatedMap.keySet()) {
            if (lower.startsWith(key) || key.startsWith(lower)) {
                return prepopulatedMap.get(key);
            }
        }
        return List.of();
    }
}