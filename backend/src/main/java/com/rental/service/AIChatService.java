package com.rental.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.dto.PropertyResponseDto;
import com.rental.dto.SearchFilters;
import com.rental.dto.VoiceSearchResponse;
import com.rental.entity.Location;
import com.rental.entity.Property;
import com.rental.mapper.PropertyMapper;
import com.rental.repository.LocationRepository;
import com.rental.repository.PropertyRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatService {

    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?:under|below|<|less than)\\s*[₹Rs.]?\\s*(\\d+[,.]?\\d*)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile(
        "(?:above|over|>|more than)\\s*[₹Rs.]?\\s*(\\d+[,.]?\\d*)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PRICE_RANGE_PATTERN = Pattern.compile(
        "(?:between)\\s*[₹Rs.]?\\s*(\\d+[,.]?\\d*)\\s*(?:and|to|\\-)\\s*[₹Rs.]?\\s*(\\d+[,.]?\\d*)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BEDROOM_PATTERN = Pattern.compile(
        "(\\d+)\\s*(?:BHK|bedroom|bed|bhk)",
        Pattern.CASE_INSENSITIVE
    );

    // ✅ Default page size for voice search results
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final OllamaService ollamaService;
    private final PropertyRepository propertyRepository;
    private final LocationRepository locationRepository;
    private final PropertyMapper propertyMapper;
    private final LocationManagementService locationManagementService;
    private final ObjectMapper objectMapper;

    private Set<String> cachedLocationNames;

    @Value("${app.voice-search.fallback-enabled:true}")
    private boolean fallbackEnabled;

    @Value("${app.voice-search.ai-timeout-ms:300}")
    private int aiTimeoutMs;

    @PostConstruct
    public void init() {
        refreshLocationCache();
    }

    private void refreshLocationCache() {
        try {
            List<Location> activeLocations = locationRepository.findAllByIsActiveTrueOrderByDisplayOrderAscDisplayNameAsc();
            cachedLocationNames = activeLocations.stream()
                    .map(Location::getDisplayName)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            log.info("📍 Location cache refreshed: {} locations loaded", cachedLocationNames.size());
        } catch (Exception e) {
            log.error("❌ Failed to refresh location cache", e);
            cachedLocationNames = Set.of();
        }
    }

    public VoiceSearchResponse processVoiceSearch(String query) {
        log.info("🔍 ===== START Voice Search =====");
        log.info("📝 Query: {}", query);

        long regexStart = System.currentTimeMillis();
        SearchFilters regexFilters = extractFiltersWithRegex(query);
        long regexDuration = System.currentTimeMillis() - regexStart;
        log.info("⏱️ Step 1 - Regex extraction: {}ms", regexDuration);
        log.info("📊 Regex filters: {}", regexFilters);

        SearchFilters finalFilters = regexFilters;
        String explanation = null;
        boolean aiAvailable = false;
        boolean aiUsed = false;
        long aiDuration = 0;

        long aiCheckStart = System.currentTimeMillis();
        try {
            aiAvailable = ollamaService.isOllamaRunning();
            long aiCheckDuration = System.currentTimeMillis() - aiCheckStart;
            log.info("⏱️ Step 2 - AI availability check: {}ms (available: {})", aiCheckDuration, aiAvailable);

            if (aiAvailable && fallbackEnabled) {
                log.info("🤖 AI available, attempting extraction with {}ms timeout", aiTimeoutMs);

                long aiExtractStart = System.currentTimeMillis();

                CompletableFuture<SearchFilters> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("🧠 AI extraction started...");
                        return extractFiltersWithAI(query);
                    } catch (Exception e) {
                        log.warn("⚠️ AI extraction failed: {}", e.getMessage());
                        return null;
                    }
                });

                SearchFilters aiFilters = null;
                try {
                    aiFilters = future.get(aiTimeoutMs, TimeUnit.MILLISECONDS);
                    log.info("✅ AI responded within {}ms", aiTimeoutMs);
                } catch (TimeoutException e) {
                    log.info("⏱️ AI TIMED OUT after {}ms - using regex results", aiTimeoutMs);
                    future.cancel(true);
                } catch (Exception e) {
                    log.warn("⚠️ AI extraction error: {}", e.getMessage());
                }

                aiDuration = System.currentTimeMillis() - aiExtractStart;
                log.info("⏱️ Step 3 - AI extraction: {}ms (timeout: {}ms)", aiDuration, aiTimeoutMs);

                if (aiFilters != null && !hasNoValidFilters(aiFilters)) {
                    log.info("✅ AI extracted filters successfully: {}", aiFilters);
                    finalFilters = aiFilters;
                    aiUsed = true;
                    explanation = "AI understood your search";
                } else {
                    log.info("⚡ Using regex filters (AI: {})",
                        aiFilters == null ? "timed out after " + aiTimeoutMs + "ms" : "invalid response");
                    if (explanation == null) {
                        explanation = "AI couldn't fully understand, using basic search.";
                    }
                }
            } else if (!aiAvailable) {
                explanation = "AI service is not available. Using basic search.";
            }
        } catch (Exception e) {
            log.error("❌ AI processing error: {}", e.getMessage());
            if (explanation == null) {
                explanation = "Search service encountered an issue. Using basic search.";
            }
        }

        long validateStart = System.currentTimeMillis();
        SearchFilters validatedFilters = validateAndCleanFilters(finalFilters);
        long validateDuration = System.currentTimeMillis() - validateStart;
        log.info("⏱️ Step 4 - Validate & clean filters: {}ms", validateDuration);
        log.info("📊 Final filters: {}", validatedFilters);

        if (explanation == null && validatedFilters.getExplanation() != null) {
            explanation = validatedFilters.getExplanation();
        }

        // ✅ OPTIMIZED: Use database query with pagination instead of in-memory filtering
        long searchStart = System.currentTimeMillis();
        
        // Create pageable (default page 0, size 50)
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
        
        // Use the existing repository method that does database-level filtering
        Page<Property> propertyPage = propertyRepository.findVisibleWithFilters(
            null,                                    // ownerId (null for public search)
            null,                                    // role (null for public search)
            validatedFilters.getLocationId(),        // locationId
            validatedFilters.getMinRent(),           // minPrice
            validatedFilters.getMaxRent(),           // maxPrice
            validatedFilters.getBedrooms(),          // bedrooms
            validatedFilters.getAmenityIds(),        // amenities
            pageable
        );
        
        long searchDuration = System.currentTimeMillis() - searchStart;
        log.info("⏱️ Step 5 - Database search: {}ms (found {} properties out of {})", 
            searchDuration, propertyPage.getNumberOfElements(), propertyPage.getTotalElements());

        long dtoStart = System.currentTimeMillis();
        List<PropertyResponseDto> propertyDtos = propertyPage.getContent().stream()
                .map(propertyMapper::toDto)
                .collect(Collectors.toList());
        long dtoDuration = System.currentTimeMillis() - dtoStart;
        log.info("⏱️ Step 6 - DTO conversion: {}ms", dtoDuration);

        if (explanation == null) {
            explanation = generateExplanation(propertyPage.getTotalElements(), validatedFilters);
        }

        long totalDuration = System.currentTimeMillis() - regexStart;

        log.info("⏱️ ===== VOICE SEARCH SUMMARY =====");
        log.info("📊 Total: {}ms", totalDuration);
        log.info("📊 Regex: {}ms, AI: {}ms, Validate: {}ms, Search: {}ms, DTO: {}ms",
            regexDuration, aiDuration, validateDuration, searchDuration, dtoDuration);
        log.info("🤖 AI Used: {}, AI Available: {}, Total Results: {}", aiUsed, aiAvailable, propertyPage.getTotalElements());
        log.info("📝 Query: {}", query);
        log.info("🔍 ===== END Voice Search =====");

        return VoiceSearchResponse.builder()
                .transcript(query)
                .explanation(explanation)
                .filters(validatedFilters)
                .properties(propertyDtos)
                .totalResults(propertyPage.getTotalElements())
                .aiAvailable(aiAvailable)
                .build();
    }

    private SearchFilters extractFiltersWithAI(String query) {
        long start = System.currentTimeMillis();

        String locationList = String.join(", ", cachedLocationNames);
        if (locationList.isEmpty()) {
            locationList = "Bangalore, Mumbai, Hyderabad, Chennai, Delhi";
        }

        String prompt =
            "Extract rental search filters from this query: \"" + query + "\"\n\n" +
            "Return ONLY valid JSON with these fields (use null if not mentioned):\n" +
            "- location: city name (must be one of: " + locationList + ")\n" +
            "- minRent: number (minimum rent)\n" +
            "- maxRent: number (maximum rent)\n" +
            "- bedrooms: number (1, 2, 3, etc.)\n" +
            "- amenities: array of strings (parking, metro, gym, pool, garden, furnished, ac)\n\n" +
            "Examples:\n" +
            "Query: '2 BHK in Bangalore under 25000'\n" +
            "Response: {\"location\":\"Bangalore\",\"minRent\":null,\"maxRent\":25000,\"bedrooms\":2,\"amenities\":null}\n\n" +
            "Query: 'apartment in Chennai between 15000 and 30000'\n" +
            "Response: {\"location\":\"Chennai\",\"minRent\":15000,\"maxRent\":30000,\"bedrooms\":null,\"amenities\":null}\n\n" +
            "Query: '1 BHK with parking near metro in Hyderabad'\n" +
            "Response: {\"location\":\"Hyderabad\",\"minRent\":null,\"maxRent\":null,\"bedrooms\":1,\"amenities\":[\"parking\",\"metro\"]}\n\n" +
            "NOW extract from: \"" + query + "\"\n" +
            "Return ONLY JSON, no other text.";

        try {
            String aiResponse = ollamaService.generateResponse(prompt);

            String cleanResponse = aiResponse
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .replaceAll("^\\s*", "")
                .replaceAll("\\s*$", "");

            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(cleanResponse);

            SearchFilters filters = new SearchFilters();

            if (jsonNode.has("location") && !jsonNode.get("location").isNull()) {
                String locationName = jsonNode.get("location").asText();
                Long locationId = locationManagementService.getLocationIdByName(locationName);
                if (locationId != null) {
                    filters.setLocationId(locationId);
                }
            }

            if (jsonNode.has("minRent") && !jsonNode.get("minRent").isNull()) {
                filters.setMinRent(jsonNode.get("minRent").asDouble());
            }

            if (jsonNode.has("maxRent") && !jsonNode.get("maxRent").isNull()) {
                filters.setMaxRent(jsonNode.get("maxRent").asDouble());
            }

            if (jsonNode.has("bedrooms") && !jsonNode.get("bedrooms").isNull()) {
                filters.setBedrooms(jsonNode.get("bedrooms").asInt());
            }

            String explanation = generateAIExplanation(query, filters);
            filters.setExplanation(explanation);

            long totalDuration = System.currentTimeMillis() - start;
            log.info("⏱️ AI extraction successful in {}ms", totalDuration);

            return filters;

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - start;
            log.warn("⚠️ AI extraction failed after {}ms: {}", totalDuration, e.getMessage());
            return SearchFilters.builder()
                    .explanation("Sorry, I couldn't understand your query. Please try again.")
                    .build();
        }
    }

    private String generateAIExplanation(String query, SearchFilters filters) {
        StringBuilder explanation = new StringBuilder("Searching for ");

        if (filters.getBedrooms() != null) {
            explanation.append(filters.getBedrooms()).append(" BHK ");
        }

        if (filters.getMinRent() != null && filters.getMaxRent() != null) {
            explanation.append("between ₹").append(String.format("%.0f", filters.getMinRent()))
                     .append(" and ₹").append(String.format("%.0f", filters.getMaxRent()));
        } else if (filters.getMaxRent() != null) {
            explanation.append("under ₹").append(String.format("%.0f", filters.getMaxRent()));
        } else if (filters.getMinRent() != null) {
            explanation.append("above ₹").append(String.format("%.0f", filters.getMinRent()));
        }

        return explanation.toString();
    }

    private SearchFilters extractFiltersWithRegex(String query) {
        String lowerQuery = query.toLowerCase();
        SearchFilters.SearchFiltersBuilder builder = SearchFilters.builder();
        log.info("🔍 Extracting from: {}", lowerQuery);

        String[] words = lowerQuery.split("\\s+");
        for (String word : words) {
            Long locationId = locationManagementService.getLocationIdByName(word);
            if (locationId != null) {
                builder.locationId(locationId);
                log.info("📍 Extracted location: {} (ID: {})", word, locationId);
                break;
            }
        }

        Matcher rangeMatcher = PRICE_RANGE_PATTERN.matcher(lowerQuery);
        if (rangeMatcher.find()) {
            String minPriceStr = rangeMatcher.group(1).replace(",", "");
            String maxPriceStr = rangeMatcher.group(2).replace(",", "");
            try {
                builder.minRent(Double.parseDouble(minPriceStr));
                builder.maxRent(Double.parseDouble(maxPriceStr));
                log.info("💰 Extracted price range: {} - {}", minPriceStr, maxPriceStr);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        if (builder.build().getMaxRent() == null) {
            Matcher priceMatcher = PRICE_PATTERN.matcher(lowerQuery);
            if (priceMatcher.find()) {
                String priceStr = priceMatcher.group(1).replace(",", "");
                try {
                    builder.maxRent(Double.parseDouble(priceStr));
                    log.info("💰 Extracted max rent: {}", priceStr);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        if (builder.build().getMinRent() == null) {
            Matcher minPriceMatcher = MIN_PRICE_PATTERN.matcher(lowerQuery);
            if (minPriceMatcher.find()) {
                String priceStr = minPriceMatcher.group(1).replace(",", "");
                try {
                    builder.minRent(Double.parseDouble(priceStr));
                    log.info("💰 Extracted min rent: {}", priceStr);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        Matcher bedroomMatcher = BEDROOM_PATTERN.matcher(lowerQuery);
        if (bedroomMatcher.find()) {
            try {
                builder.bedrooms(Integer.parseInt(bedroomMatcher.group(1)));
                log.info("🛏️ Extracted bedrooms: {}", bedroomMatcher.group(1));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        builder.explanation("Extracted from: " + query);
        return builder.build();
    }

    private boolean hasNoValidFilters(SearchFilters filters) {
        return filters == null ||
               (filters.getLocationId() == null &&
                filters.getMinRent() == null &&
                filters.getMaxRent() == null &&
                filters.getBedrooms() == null &&
                (filters.getAmenityIds() == null || filters.getAmenityIds().isEmpty()));
    }

    private SearchFilters validateAndCleanFilters(SearchFilters filters) {
        if (filters.getMinRent() != null && filters.getMaxRent() != null) {
            if (filters.getMinRent() > filters.getMaxRent()) {
                log.warn("⚠️ Invalid rent range: swapping values");
                Double temp = filters.getMinRent();
                filters.setMinRent(filters.getMaxRent());
                filters.setMaxRent(temp);
            }
        }

        if (filters.getAmenityIds() != null) {
            List<Long> cleanedAmenities = filters.getAmenityIds().stream()
                    .filter(a -> a != null)
                    .collect(Collectors.toList());
            filters.setAmenityIds(cleanedAmenities.isEmpty() ? null : cleanedAmenities);
        }

        if (filters.getLocationId() == null && filters.getMinRent() == null &&
            filters.getMaxRent() == null && filters.getBedrooms() == null &&
            filters.getAmenityIds() == null) {
            filters.setExplanation("No valid search filters found. Please try a different query.");
        }

        return filters;
    }

    private String generateExplanation(long totalElements, SearchFilters filters) {
        if (totalElements == 0) {
            return "No properties found matching your criteria. Try adjusting your search.";
        }
        return String.format("Found %d properties that match your needs.", totalElements);
    }

    public boolean isAvailable() {
        return ollamaService.isOllamaRunning();
    }
}